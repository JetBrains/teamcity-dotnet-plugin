package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.runners.FileSystemService
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class LoggerResolverTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // INTEGRATION_PACKAGE_HOME runner parameter is not specified
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService(),
                        emptyMap<String, String>(),
                        null,
                        null),

                // Success scenario for defaults
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService().addFile(File(File("home", "msbuild15"), "TeamCity.MSBuild.Logger.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        File(File("home", "msbuild15"), "TeamCity.MSBuild.Logger.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                // Success scenario

                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService().addFile(File(File("home", "msbuild12"), "TeamCity.MSBuild.Logger.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest12Windows.id),
                        File(File("home", "msbuild12"), "TeamCity.MSBuild.Logger.dll"),
                        null),

                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService().addFile(File(File("home", "msbuild12"), "TeamCity.MSBuild.Logger.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest12Windows.id),
                        File(File("home", "msbuild12"), "TeamCity.MSBuild.Logger.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest12"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest12Windows.id),
                        File(File("home", "vstest12"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest12"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild12WindowsX64.id),
                        File(File("home", "vstest12"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15CrossPlatform.id),
                        File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15CrossPlatform.id),
                        File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest14"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id),
                        File(File("home", "vstest14"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest14"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id, DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id),
                        File(File("home", "vstest14"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService().addFile(File(File("home", "msbuild14"), "TeamCity.MSBuild.Logger.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id, DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id),
                        File(File("home", "msbuild14"), "TeamCity.MSBuild.Logger.dll"),
                        null),

                // Has no assembly
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService().addDirectory(File("home", "msbuild15")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        null,
                        "Path \".+\" to MSBuild logger was not found" as String?),

                // Has no directory
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService(),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        null,
                        "Path \".+\" to integration pack was not found" as String?)
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            toolType: ToolType,
            fileSystemService: FileSystemService,
            parameters: Map<String, String>,
            expectedLogger: File?,
            expectedErrorPattern: String?) {
        // Given
        val loggerProvider = LoggerResolverImpl(ParametersServiceStub(parameters), fileSystemService)

        // When
        var actualLogger: File? = null;
        try {
            actualLogger = loggerProvider.resolve(toolType)
        }
        catch (ex: RunBuildException) {
            if(expectedErrorPattern != null) {
                Assert.assertEquals(Regex(expectedErrorPattern).matches(ex.message as String), true)
            }
            else {
                Assert.fail("Unexpected exception ${ex}")
            }
        }

        // Then
        if(actualLogger != null) {
            Assert.assertEquals(actualLogger, expectedLogger)
        }
    }
}