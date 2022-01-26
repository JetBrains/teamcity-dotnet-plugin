/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildToolResolverTest {
    @MockK private lateinit var _virtualContext: VirtualContext
    private val _msbuildStateWorkflowComposer: ToolStateWorkflowComposer = mockk<ToolStateWorkflowComposer>()
    private val _dotnetStateWorkflowComposer: ToolStateWorkflowComposer = mockk<ToolStateWorkflowComposer>()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild12WindowsX86.id, "MSBuildTools12.0_x86_Path" to "msbuild12X86"), File("msbuild12X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id, "MSBuildTools14.0_x86_Path" to "msbuild14X86"), File("msbuild14X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX86.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),

                // Select x86 when has x64 and x86
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),

                // Select x64 when has x64 and x86 and PARAM_DEFAULT_BITNESS=x64
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_DEFAULT_BITNESS to "x64", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_DEFAULT_BITNESS to "X64", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),

                // Select x86 when has x64 and x86 and PARAM_DEFAULT_BITNESS=x86 or any not x64
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_DEFAULT_BITNESS to "x86", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_DEFAULT_BITNESS to "X86", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_DEFAULT_BITNESS to "abc", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),

                // Select x64 when has x64 only
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_DEFAULT_BITNESS to "x86", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),

                // Select x86 when has x86 only
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_DEFAULT_BITNESS to "x64", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null, _msbuildStateWorkflowComposer),

                // Docker
                arrayOf(true, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX86.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86"), File(MSBuildToolResolver.MSBuildWindowsTooName), false, null, _msbuildStateWorkflowComposer),

                // Mono
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildMono.id, MonoConstants.CONFIG_PATH to "mono"), File(File("mono").absoluteFile.parent, MSBuildToolResolver.MSBuildMonoWindowsToolName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.UNIX, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildMono.id, MonoConstants.CONFIG_PATH to "mono"), File(File("mono").absoluteFile.parent, MSBuildToolResolver.MSBuildMonoToolName).absoluteFile, false, null, _msbuildStateWorkflowComposer),
                arrayOf(false, OSType.MAC, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildMono.id, MonoConstants.CONFIG_PATH to "mono"), File(File("mono").absoluteFile.parent, MSBuildToolResolver.MSBuildMonoToolName).absoluteFile, false, null, _msbuildStateWorkflowComposer),

                // Mono in docker
                arrayOf(true, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildMono.id, MonoConstants.CONFIG_PATH to "mono"), File(MSBuildToolResolver.MSBuildMonoWindowsToolName), false, null, _msbuildStateWorkflowComposer),
                arrayOf(true, OSType.UNIX, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildMono.id, MonoConstants.CONFIG_PATH to "mono"), File(MSBuildToolResolver.MSBuildMonoToolName), false, null, _msbuildStateWorkflowComposer),
                arrayOf(true, OSType.MAC, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildMono.id, MonoConstants.CONFIG_PATH to "mono"), File(MSBuildToolResolver.MSBuildMonoToolName), false, null, _msbuildStateWorkflowComposer),

                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildCrossPlatform.id), File("dotnet"), true, null, _dotnetStateWorkflowComposer),
                arrayOf(false, OSType.UNIX, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildCrossPlatform.id), File("dotnet"), true, null, _dotnetStateWorkflowComposer),
                arrayOf(false, OSType.MAC, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildCrossPlatform.id), File("dotnet"), true, null, _dotnetStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, emptyMap<String, String>(), File("dotnet"), true, null, _dotnetStateWorkflowComposer),
                arrayOf(false, OSType.UNIX, emptyMap<String, String>(), File("dotnet"), true, null, _dotnetStateWorkflowComposer),
                arrayOf(false, OSType.MAC, emptyMap<String, String>(), File("dotnet"), true, null, _dotnetStateWorkflowComposer),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX86.id), File(""), null, Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: MSBuildTools15.0_x86_Path"), null),
                arrayOf(false, OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id), File(""), null, Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: MSBuildTools15.0_x64_Path"), null))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideExecutableFile(
            isVirual: Boolean,
            targetOSType: OSType,
            parameters: Map<String, String>,
            expectedExecutableFile: File,
            expectedIsCommandRequired: Boolean?,
            exceptionPattern: Regex?,
            expectedToolStateWorkflowComposer: ToolStateWorkflowComposer?) {

        // Given
        val instance = createInstance(parameters, File("dotnet"))

        // When
        every { _virtualContext.isVirtual } returns isVirual
        every { _virtualContext.targetOSType } returns targetOSType

        var actualIsCommandRequired: Boolean? = null
        var actualExecutable: ToolPath? = null
        try {
            actualExecutable = instance.executable
            actualIsCommandRequired = instance.isCommandRequired
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        } catch (ex: RunBuildException) {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true)
        }

        // Then
        if (exceptionPattern == null) {
            Assert.assertEquals(instance.toolStateWorkflowComposer, expectedToolStateWorkflowComposer)
            Assert.assertEquals(actualExecutable, ToolPath(Path(expectedExecutableFile.path)))
            Assert.assertEquals(actualIsCommandRequired, expectedIsCommandRequired)
        }
    }

    private fun createInstance(parameters: Map<String, String>, executableFile: File): ToolResolver {
        return MSBuildToolResolver(
                _virtualContext,
                ParametersServiceStub(parameters),
                ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path(executableFile.path)), true, _dotnetStateWorkflowComposer), _msbuildStateWorkflowComposer)
    }
}