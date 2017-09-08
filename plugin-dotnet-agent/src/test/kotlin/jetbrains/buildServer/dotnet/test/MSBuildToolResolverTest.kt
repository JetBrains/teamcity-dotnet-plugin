package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.runners.ParametersService
import jetbrains.buildServer.runners.PathsService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildToolResolverTest {
    private var _pathsService: PathsService? = null;

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildTooName).absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild12WindowsX86.id, "MSBuildTools12.0_x86_Path" to "msbuild12X86"), File("msbuild12X86", MSBuildToolResolver.MSBuildTooName).absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id, "MSBuildTools14.0_x86_Path" to "msbuild14X86"), File("msbuild14X86", MSBuildToolResolver.MSBuildTooName).absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildTooName).absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86", "MSBuildTools15.0_x64_Path" to "msbuild15X64"), File("msbuild15X64", MSBuildToolResolver.MSBuildTooName).absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id, "MSBuildTools15.0_x86_Path" to "msbuild15X86"), File("msbuild15X86", MSBuildToolResolver.MSBuildTooName).absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15CrossPlatform.id), File("dotnet"), null),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15Windows.id), File(""), Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: MSBuildTools15.0_x64_Path")),
                arrayOf(mapOf(DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id), File(""), Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: MSBuildTools15.0_x64_Path")))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideExecutableFile(
            parameters: Map<String, String>,
            expectedExecutableFile: File,
            exceptionPattern: Regex?) {
        // Given
        val instance = createInstance(parameters, File("dotnet"))

        // When
        var actualExecutableFile: File? = null;
        try {
            actualExecutableFile = instance.executableFile
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        }
        catch (ex: RunBuildException)
        {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true);
        }


        // Then
        if (exceptionPattern == null) {
            Assert.assertEquals(actualExecutableFile, expectedExecutableFile)
        }
    }

    private fun createInstance(parameters: Map<String, String>, executableFile: File): ToolResolver {
        return MSBuildToolResolver(ParametersServiceStub(parameters), DotnetToolResolverStub(executableFile, true))
    }
}