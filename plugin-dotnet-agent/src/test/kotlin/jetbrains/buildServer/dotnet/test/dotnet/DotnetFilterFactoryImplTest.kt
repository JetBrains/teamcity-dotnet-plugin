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

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetFilterFactoryImplTest {
    @MockK private lateinit var _testsFilterProvider: TestsFilterProvider
    @MockK private lateinit var _testsSplittingSettings: TestsSplittingSettings
    @MockK private lateinit var _testRunSettingsFileProvider: TestRunSettingsFileProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    public fun `should create filter using settings file when filter size bigger than limit`() {
        // Given
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)
        val settingsFile = File("My.settings")

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _testsSplittingSettings.mode } returns TestsSplittingMode.TestClassNameFilter
        every { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) } returns settingsFile
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter("", settingsFile, true))
    }

    @Test
    public fun `should use filter as argument when cannot generate settings file`() {
        // Given
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _testsSplittingSettings.mode } returns TestsSplittingMode.TestClassNameFilter
        every { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) } returns null
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null, true))
    }

    @Test
    public fun `should use filter as argument when filter is less or eq the limit`() {
        // Given
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize)

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _testsSplittingSettings.mode } returns TestsSplittingMode.TestClassNameFilter
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null, true))
        verify(exactly = 0) { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) }
    }

    @Test
    public fun `should use filter as argument when no test splitting`() {
        // Given
        val factory = createInstance()
        val filter = "a".repeat(DotnetFilterFactoryImpl.MaxArgSize + 1)

        // When
        every { _testsFilterProvider.filterExpression } returns filter
        every { _testsSplittingSettings.mode } returns TestsSplittingMode.Disabled
        val actualFilter = factory.createFilter(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFilter, DotnetFilter(filter, null, false))
        verify(exactly = 0) { _testRunSettingsFileProvider.tryGet(DotnetCommandType.Test) }
    }

    private fun createInstance() = DotnetFilterFactoryImpl(_testsFilterProvider, _testsSplittingSettings, _testRunSettingsFileProvider)
}