package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.LoggerResolverImpl
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.ToolType
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class LoggerResolverTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Get bundled when INTEGRATION_PACKAGE_HOME runner parameter is not specified
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService()
                                .addFile(File(File(File(ToolsPath, "TeamCity.Dotnet.Integration.1.0.34"), "msbuild15"), "TeamCity.MSBuild.Logger.dll")),
                        emptyMap<String, String>(),
                        File(File(File(ToolsPath, "TeamCity.Dotnet.Integration.1.0.34"), "msbuild15"), "TeamCity.MSBuild.Logger.dll"),
                        null),

                // when bundled was not found
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService(),
                        emptyMap<String, String>(),
                        null,
                        ".NET integration package was not found at .+" as String?),

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
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15DotnetCore.id),
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

                // Use bundled when has no directory
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService().addFile(File(File(File(ToolsPath, "TeamCity.Dotnet.Integration.1.0.34"), "msbuild15"), "TeamCity.MSBuild.Logger.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        File(File(File(ToolsPath, "TeamCity.Dotnet.Integration.1.0.34"), "msbuild15"), "TeamCity.MSBuild.Logger.dll"),
                        null)
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
        val pluginPath = File("plugin")
        val ctx = Mockery()
        val pathsService = ctx.mock(PathsService::class.java)
        ctx.checking(object : Expectations() {
            init {
                allowing<PathsService>(pathsService).getPath(PathType.Plugin)
                will(returnValue(pluginPath))
            }
        })

        val loggerProvider = LoggerResolverImpl(ParametersServiceStub(parameters), fileSystemService, pathsService)

        // When
        var actualLogger: File? = null
        try {
            actualLogger = loggerProvider.resolve(toolType)
        } catch (ex: RunBuildException) {
            if (expectedErrorPattern != null) {
                Assert.assertEquals(Regex(expectedErrorPattern).matches(ex.message as String), true)
            } else {
                Assert.fail("Unexpected exception $ex")
            }
        }

        // Then
        if (actualLogger != null) {
            Assert.assertEquals(actualLogger, expectedLogger)
        }
    }

    companion object {
        private val PluginPath = File("plugin")
        private val ToolsPath = File(PluginPath, LoggerResolverImpl.ToolsDirectoryName)
    }
}