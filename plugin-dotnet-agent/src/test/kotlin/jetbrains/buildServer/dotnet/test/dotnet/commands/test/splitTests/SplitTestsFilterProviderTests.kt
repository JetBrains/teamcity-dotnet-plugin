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

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitTests

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterSettings
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterType
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterType.Excludes
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterType.Includes
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesReader
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SplitTestsFilterProviderTests {
    @MockK
    private lateinit var _settingsMock: SplitTestsFilterSettings

    @MockK
    private lateinit var _testsNamesReaderMock: SplitTestsNamesReader

    @MockK
    private lateinit var _loggerMock: Logger

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkObject(Logger)
        every { Logger.getLogger(any()) } returns _loggerMock
        every { _settingsMock.trimTestClassParameters } returns false
        justRun { _loggerMock.debug(any<String>()) }
        justRun { _loggerMock.warn(any<String>()) }
    }

    @Test
    fun `should provide an empty filter expression if test spiltting disabled`() {
        // arrange
        every { _settingsMock.isActive } answers { false }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "")
        verify(exactly = 0) { _loggerMock.debug(any<String>()) }
        verify(exactly = 0) { _loggerMock.warn(any<String>()) }
    }

    @DataProvider
    fun testDataForDefaultFilterExpression() = arrayOf(
        arrayOf(
            Includes,
            sequenceOf("Namespace.TestClass0", "Namespace.TestClass1"),
            "FullyQualifiedName~Namespace.TestClass0. | FullyQualifiedName~Namespace.TestClass1."
        ),
        arrayOf(
            Excludes,
            sequenceOf("Namespace.TestClass0", "Namespace.TestClass1"),
            "FullyQualifiedName!~Namespace.TestClass0. & FullyQualifiedName!~Namespace.TestClass1."
        ),
    )

    @Test(dataProvider = "testDataForDefaultFilterExpression")
    fun `should provide default filter expression`(
        filterType: SplitTestsFilterType,
        names: Sequence<String>,
        expectedFilterExpression: String
    ) {
        // arrange
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.testClasses } answers { names }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, expectedFilterExpression)
    }

    @DataProvider
    fun testDataForDefaultFilterExpressionWithMoreThan1000TestClasses() = arrayOf(
        arrayOf(Includes, "^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$"),
        arrayOf(Excludes, "^\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\$"),
    )

    @Test(dataProvider = "testDataForDefaultFilterExpressionWithMoreThan1000TestClasses")
    fun `should provide default filter expression for more than 1000 test classes`(
        filterType: SplitTestsFilterType,
        regex: String
    ) {
        // arrange
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2500) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex(regex).matches(result))
    }

    @Test
    fun `should provide exact match filter expression`() {
        // arrange
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { true }
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
    fun `should provide exact math filter expression for more than 1000 test names`() {
        // arrange
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { true }
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(25, 100) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$").matches(result))
    }

    @DataProvider
    fun testDataForParametrisedTestClasses() = arrayOf(
        arrayOf(true, Includes, "FullyQualifiedName~Namespace.TestClass0. | FullyQualifiedName~Namespace.TestClass1."),
        arrayOf(false, Includes, """FullyQualifiedName~Namespace.TestClass0("param"). | FullyQualifiedName~Namespace.TestClass1("param")."""),
        arrayOf(true, Excludes, "FullyQualifiedName!~Namespace.TestClass0. & FullyQualifiedName!~Namespace.TestClass1."),
        arrayOf(false, Excludes, """FullyQualifiedName!~Namespace.TestClass0("param"). & FullyQualifiedName!~Namespace.TestClass1("param")."""),
    )

    @Test(dataProvider = "testDataForParametrisedTestClasses")
    fun `should provide default filter expression processing test class parameters`(
        shouldTrimTestClassParameters: Boolean,
        filterType: SplitTestsFilterType,
        expectedFilterExpression: String
    ) {
        // arrange
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.trimTestClassParameters } answers { shouldTrimTestClassParameters }
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.testClasses } answers { generateTestClassesWithParamsList(2) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, expectedFilterExpression)
    }

    @DataProvider
    fun testDataForTrimmingTestClassParameters() = arrayOf(
        arrayOf("TestClass", "FullyQualifiedName~TestClass."),
        arrayOf("TestClass(param", "FullyQualifiedName~TestClass(param."),
        arrayOf("Namespace.TestClass", "FullyQualifiedName~Namespace.TestClass."),
        arrayOf("Namespace.TestClass(param", "FullyQualifiedName~Namespace.TestClass(param."),
        arrayOf("Namespace.TestClass(param1,param2)", "FullyQualifiedName~Namespace.TestClass."),
        arrayOf("Namespace1.Namespace2.TestClass(param1,param2)", "FullyQualifiedName~Namespace1.Namespace2.TestClass."),
        arrayOf("Namespace.TestClass((param))", "FullyQualifiedName~Namespace.TestClass."),
        arrayOf("Namespace.TestCla(ss(param)", "FullyQualifiedName~Namespace.TestCla."),
    )

    @Test(dataProvider = "testDataForTrimmingTestClassParameters")
    fun `should provide default filter expression processing test class parameters`(
        sourceName: String,
        expectedFilterExpression: String
    ) {
        // arrange
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.trimTestClassParameters } answers { true }
        every { _settingsMock.filterType } answers { Includes }
        every { _settingsMock.testClasses } answers { sequenceOf(sourceName) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, expectedFilterExpression)
    }

    private fun create() =
        SplitTestsFilterProvider(_settingsMock, _testsNamesReaderMock)

    private fun generateTestClassesList(n: Int) = sequence {
        for (index in 0 until n) {
            yield("Namespace.TestClass$index")
        }
    }

    private fun generateTestClassesWithParamsList(n: Int) = sequence {
        for (index in 0 until n) {
            yield("""Namespace.TestClass$index("param")""")
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
