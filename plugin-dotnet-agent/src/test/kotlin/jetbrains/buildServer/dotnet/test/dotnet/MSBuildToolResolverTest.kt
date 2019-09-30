package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildToolResolverTest {
    private lateinit var _ctx: Mockery
    private lateinit var _environment: Environment

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _environment = _ctx.mock(Environment::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild12WindowsX86.id, "MSBuildTools12.0_x86_Path" to "msbuild12X86"), File("msbuild12X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id, "MSBuildTools14.0_x86_Path" to "msbuild14X86"), File("msbuild14X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86"), File("msbuild15X86", MSBuildToolResolver.MSBuildWindowsTooName).absoluteFile, false, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Mono.id, MonoConstants.CONFIG_PATH to "mono"), File(File("mono").absoluteFile.parent, MSBuildToolResolver.MSBuildMonoWindowsToolName).absoluteFile, false, null),
                arrayOf(OSType.UNIX, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Mono.id, MonoConstants.CONFIG_PATH to "mono"), File(File("mono").absoluteFile.parent, MSBuildToolResolver.MSBuildMonoToolName).absoluteFile, false, null),
                arrayOf(OSType.MAC, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Mono.id, MonoConstants.CONFIG_PATH to "mono"), File(File("mono").absoluteFile.parent, MSBuildToolResolver.MSBuildMonoToolName).absoluteFile, false, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15DotnetCore.id), File("dotnet"), true, null),
                arrayOf(OSType.UNIX, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15DotnetCore.id), File("dotnet"), true, null),
                arrayOf(OSType.MAC, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15DotnetCore.id), File("dotnet"), true, null),
                arrayOf(OSType.WINDOWS, emptyMap<String, String>(), File("dotnet"), true, null),
                arrayOf(OSType.UNIX, emptyMap<String, String>(), File("dotnet"), true, null),
                arrayOf(OSType.MAC, emptyMap<String, String>(), File("dotnet"), true, null),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id), File(""), null, Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: MSBuildTools15.0_x64_Path")),
                arrayOf(OSType.WINDOWS, mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id), File(""), null, Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: MSBuildTools15.0_x64_Path")))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideExecutableFile(
            os: OSType,
            parameters: Map<String, String>,
            expectedExecutableFile: File,
            expectedIsCommandRequired: Boolean?,
            exceptionPattern: Regex?) {
        // Given
        val instance = createInstance(parameters, File("dotnet"))

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).os
                will(returnValue(os))
            }
        })


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
            Assert.assertEquals(actualExecutable, ToolPath(expectedExecutableFile))
            Assert.assertEquals(actualIsCommandRequired, expectedIsCommandRequired)
        }
    }

    private fun createInstance(parameters: Map<String, String>, executableFile: File): ToolResolver {
        return MSBuildToolResolver(
                _environment,
                ParametersServiceStub(parameters),
                DotnetToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(executableFile), true))
    }
}