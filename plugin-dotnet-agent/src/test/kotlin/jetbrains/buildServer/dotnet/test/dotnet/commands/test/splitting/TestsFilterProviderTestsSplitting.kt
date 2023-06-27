/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitting

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.splitting.*
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesReader
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestsFilterProviderTestsSplitting {
    @MockK
    private lateinit var _settingsMock: TestsSplittingSettings

    @MockK
    private lateinit var _testsNamesReaderMock: TestsSplittingByNamesReader

    @MockK
    private lateinit var _loggerMock: Logger

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkObject(Logger)
        every { Logger.getLogger(any()) } returns _loggerMock
        justRun { _loggerMock.debug(any<String>()) }
        justRun { _loggerMock.warn(any<String>()) }
    }

    @Test
    fun `should provide an empty filter expression if test spiltting disabled`() {
        // arrange
        every { _settingsMock.mode } answers { TestsSplittingMode.Disabled }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "")
        verify (exactly = 0) { _loggerMock.debug(any<String>()) }
        verify (exactly = 0) { _loggerMock.warn(any<String>()) }
    }

    @Test
    fun `should provide test class filter expression for includes filter type`() {
        // arrange
        every { _settingsMock.mode } answers { TestsSplittingMode.TestClassNameFilter }
        every { _settingsMock.filterType } answers { TestsSplittingFilterType.Includes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "FullyQualifiedName~Namespace.TestClass0. | FullyQualifiedName~Namespace.TestClass1.")
    }

    @Test
    fun `should provide test class filter expression for excluded filter type`() {
        // arrange
        every { _settingsMock.mode } answers { TestsSplittingMode.TestClassNameFilter }
        every { _settingsMock.filterType } answers { TestsSplittingFilterType.Excludes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "FullyQualifiedName!~Namespace.TestClass0. & FullyQualifiedName!~Namespace.TestClass1.")
    }

    @Test
    fun `should provide test class filter expression for more than 1000 included test classes`() {
        // arrange
        every { _settingsMock.mode } answers { TestsSplittingMode.TestClassNameFilter }
        every { _settingsMock.filterType } answers { TestsSplittingFilterType.Includes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2500) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$").matches(result))
    }

    @Test
    fun `should provide test class filter expression for more than 1000 excluded test classes`() {
        // arrange
        every { _settingsMock.mode } answers { TestsSplittingMode.TestClassNameFilter }
        every { _settingsMock.filterType } answers { TestsSplittingFilterType.Excludes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2100) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\$").matches(result))
    }

    @Test
    fun `should provide test name filter expression`() {
        // arrange
        every { _settingsMock.mode } answers { TestsSplittingMode.TestNameFilter }
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(2, 2) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(
            result,
            "FullyQualifiedName=Namespace.TestClass0.Test0 | FullyQualifiedName=Namespace.TestClass0.Test1 " +
                    "| FullyQualifiedName=Namespace.TestClass1.Test0 | FullyQualifiedName=Namespace.TestClass1.Test1"
        )
    }

    @Test
    fun `should provide test name filter expression for more than 1000 test names`() {
        // arrange
        every { _settingsMock.mode } answers { TestsSplittingMode.TestNameFilter }
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(25, 100) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$").matches(result))
    }

    private fun create() =
            TestsSplittingFilterProvider(_settingsMock, _testsNamesReaderMock)

    private fun generateTestClassesList(n: Int) = sequence {
        for (index in 0 until n) {
            yield("Namespace.TestClass$index")
        }
    }

    private fun generateTestsNamesList(n: Int, m: Int) = sequence {
        for (i in 0 until n) {
            for (j in 0 until m) {
                yield("Namespace.TestClass$i.Test$j")
            }
        }
    }
}
