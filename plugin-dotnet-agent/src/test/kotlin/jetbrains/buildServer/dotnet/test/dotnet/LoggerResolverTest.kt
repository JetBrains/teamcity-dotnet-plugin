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

import io.mockk.every
import io.mockk.mockk
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
                                .addFile(File(File(ToolsPath, "msbuild15"), "TeamCity.MSBuild.Logger.dll")),
                        emptyMap<String, String>(),
                        File(File(ToolsPath, "msbuild15"), "TeamCity.MSBuild.Logger.dll"),
                        null),

                // when bundled was not found
                arrayOf(
                        ToolType.MSBuild,
                        VirtualFileSystemService(),
                        emptyMap<String, String>(),
                        null,
                        "Path \"plugin\\${File.separator}tools\\${File.separator}msbuild15\\${File.separator}TeamCity.MSBuild.Logger.dll\" to MSBuild logger was not found"),

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
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTestCrossPlatform.id),
                        File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuildCrossPlatform.id),
                        File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild15WindowsX64.id),
                        File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll"),
                        null),

                arrayOf(
                        ToolType.VSTest,
                        VirtualFileSystemService().addFile(File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home", DotnetConstants.PARAM_MSBUILD_VERSION to Tool.MSBuild14WindowsX86.id, DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id),
                        File(File("home", "vstest15"), "TeamCity.VSTest.TestAdapter.dll"),
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
                        VirtualFileSystemService().addFile(File(File(ToolsPath, "msbuild15"), "TeamCity.MSBuild.Logger.dll")),
                        mapOf(DotnetConstants.INTEGRATION_PACKAGE_HOME to "home"),
                        File(File(ToolsPath, "msbuild15"), "TeamCity.MSBuild.Logger.dll"),
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
        val pathsService = mockk<PathsService> {
            every { getPath(PathType.Plugin) } returns pluginPath
        }

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