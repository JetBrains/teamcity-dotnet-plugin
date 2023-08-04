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

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.CltConstants.CLT_PATH_PARAMETER
import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.inspect.ToolStartCommand
import jetbrains.buildServer.inspect.InspectionTool
import jetbrains.buildServer.inspect.IspectionToolPlatform
import jetbrains.buildServer.inspect.ToolStartCommandResolverImpl
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ToolStartCommandResolverTest {
    @MockK
    private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
    }

    @DataProvider
    fun resolveCases(): Array<Array<out Any?>> {
        return arrayOf(
                // Windows
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.WindowsX86.id)),
                        OSType.WINDOWS,
                        ToolStartCommand (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.x86.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Dupfinder,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.WindowsX86.id)),
                        OSType.WINDOWS,
                        ToolStartCommand (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Dupfinder.toolName).path}.x86.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to "Abc")),
                        OSType.WINDOWS,
                        ToolStartCommand(
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.WINDOWS,
                        ToolStartCommand (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.CrossPlatform.id)),
                        OSType.WINDOWS,
                        ToolStartCommand (
                                Path(""),
                                listOf(
                                        CommandLineArgument("exec"),
                                        CommandLineArgument("--runtimeconfig"),
                                        CommandLineArgument("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.runtimeconfig.json"),
                                        CommandLineArgument("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe")
                                )
                        ),
                        null
                ),

                // Unix, Mac
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.CrossPlatform.id)),
                        OSType.UNIX,
                        ToolStartCommand (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.WindowsX64.id)),
                        OSType.UNIX,
                        ToolStartCommand (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.WindowsX86.id)),
                        OSType.UNIX,
                        ToolStartCommand (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.UNIX,
                        null,
                        RunBuildException("Cannot run ${InspectionTool.Inspectcode.displayName}.")
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.MAC,
                        null,
                        RunBuildException("Cannot run ${InspectionTool.Inspectcode.displayName}.")
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.WindowsX86.id)),
                        OSType.WINDOWS,
                        null,
                        RunBuildException("Cannot find ${InspectionTool.Inspectcode.displayName}.")
                )
        )
    }

    @Test(dataProvider = "resolveCases")
    fun shouldResolve(
        tool: InspectionTool,
        parametersService: ParametersService,
        os: OSType,
        expectedProcess: ToolStartCommand?,
        expectedException: RunBuildException?) {
        // Given
        var actualProcess: ToolStartCommand? = null
        val resolver = createInstance(parametersService)
        every { _virtualContext.targetOSType } returns os

        // When
        try {
            actualProcess = resolver.resolve(tool)
        } catch (actualException: RunBuildException) {
            Assert.assertEquals(actualException.message, expectedException?.message)
        }

        // Then
        if (expectedException == null) {
            Assert.assertEquals(actualProcess, expectedProcess)
        }
    }

    private fun createInstance(parametersService: ParametersService) =
            ToolStartCommandResolverImpl(parametersService, _virtualContext)
}