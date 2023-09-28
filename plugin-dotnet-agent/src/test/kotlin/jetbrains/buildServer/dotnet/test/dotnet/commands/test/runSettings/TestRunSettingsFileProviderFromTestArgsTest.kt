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
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsFileProviderFromTestArgs
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestRunSettingsFileProviderFromTestArgsTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _commandContext: DotnetCommandContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _argumentsService.split(any()) } answers { arg<String>(0).split("_").asSequence() }
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(DotnetCommandType.Test, "-s_Abc_-s_Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.Test, "-s_Abc_-S_Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.Test, "-s_Abc_-s_Xyz  _", File("Xyz")),
                arrayOf(DotnetCommandType.Test, "-s_Abc", File("Abc")),
                arrayOf(DotnetCommandType.Test, "_  -s _  Abc    _", File("Abc")),
                arrayOf(DotnetCommandType.Test, "aaa_-s_Abc_bbb_-s_Xyz_cccc", File("Xyz")),
                arrayOf(DotnetCommandType.Test, "aaa_-s_Abc_bbb", File("Abc")),
                arrayOf(DotnetCommandType.Test, "--settings_Abc", File("Abc")),
                arrayOf(DotnetCommandType.Test, "--SettingS_Abc", File("Abc")),
                arrayOf(DotnetCommandType.Test, "aaa_--settings_Abc_bbb", File("Abc")),
                arrayOf(DotnetCommandType.Test, "aaa_--settings_Abc_bbb_--settings_Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.Test, "aaa_--settings_Abc_bbb_--settings_Xyz_cccc_dddd", File("Xyz")),
                arrayOf(DotnetCommandType.Test, "aaa_--settings_Abc_bbb_-s_Xyz_cccc_dddd", File("Xyz")),

                arrayOf(DotnetCommandType.Test, "aaa", null),
                arrayOf(DotnetCommandType.Test, "aaa_bbb", null),
                arrayOf(DotnetCommandType.VSTest, "--settings_Abc", null),
                arrayOf(DotnetCommandType.MSBuild, "--settings_Abc", null),
                arrayOf(DotnetCommandType.Test, "--_settings_Abc", null),
                arrayOf(DotnetCommandType.Test, "-settings_Abc", null),
                arrayOf(DotnetCommandType.Test, "-_s_Abc", null),
                arrayOf(DotnetCommandType.Test, "--s_Abc", null),
                arrayOf(DotnetCommandType.Test, "", null),
                arrayOf(DotnetCommandType.Test, "   _  ", null),
                arrayOf(DotnetCommandType.Test, null, null),
                arrayOf(DotnetCommandType.Test, "--settings", null),
                arrayOf(DotnetCommandType.Test, "aaa_bbb_--settings", null),
                arrayOf(DotnetCommandType.Test, "--settings_Abc_-s", null),
                arrayOf(DotnetCommandType.Test, "aaa_--settings_Abc_-s", null),
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldProvideSettingsFile(command: DotnetCommandType, runnerParamValue: String?, expectedFile: File?) {
        // Given
        val provider = createInstance()

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_ARGUMENTS) } returns runnerParamValue
        every { _commandContext.command.commandType } returns command
        val actualFile = provider.tryGet(_commandContext)

        // Then
        Assert.assertEquals(actualFile, expectedFile)
    }

    private fun createInstance() = TestRunSettingsFileProviderFromTestArgs(_parametersService, _argumentsService)
}