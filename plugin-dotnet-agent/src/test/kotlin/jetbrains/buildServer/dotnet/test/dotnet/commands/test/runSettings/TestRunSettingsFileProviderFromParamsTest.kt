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

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.runSettings

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsFileProviderFromParams
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestRunSettingsFileProviderFromParamsTest {
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(DotnetCommandType.VSTest, "Abc", File("Abc")),
                arrayOf(DotnetCommandType.Test, "Abc", File("Abc")),

                arrayOf(DotnetCommandType.MSBuild, "Abc", null),
                arrayOf(DotnetCommandType.VSTest, null, null),
                arrayOf(DotnetCommandType.VSTest, "", null),
                arrayOf(DotnetCommandType.VSTest, "  ", null)
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldProvideSettingsFile(command: DotnetCommandType, runnerParamValue: String?, expectedFile: File?) {
        // Given
        val provider = createInstance()

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_SETTINGS_FILE) } returns runnerParamValue
        val actualFile = provider.tryGet(command)

        // Then
        Assert.assertEquals(actualFile, expectedFile)
    }

    private fun createInstance() = TestRunSettingsFileProviderFromParams(_parametersService)
}