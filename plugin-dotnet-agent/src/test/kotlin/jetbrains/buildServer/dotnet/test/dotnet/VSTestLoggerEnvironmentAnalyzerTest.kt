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

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.VSTestLoggerEnvironmentAnalyzerImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestLoggerEnvironmentAnalyzerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _fileSystemService: FileSystemService
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock(PathsService::class.java)
        _fileSystemService = _ctx.mock(FileSystemService::class.java)
        _loggerService = _ctx.mock(LoggerService::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // working out of checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File("workingDir").absoluteFile,
                        emptyList<File>(),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addDirectory(File("workingDir").absoluteFile, VirtualFileSystemService.absolute(true)),
                        listOf("The directory \"${File("workingDir").absoluteFile}\" is located outside of the build checkout directory: \"${File("checkoutDir").absoluteFile}\". In this case there can be problems with running this build tests on TeamCity agent. Please refer to this issue for details: https://youtrack.jetbrains.com/issue/TW-52485")),

                // working dir is absolute inside checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File(File("checkoutDir").absoluteFile, "workingDir"),
                        emptyList<File>(),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addDirectory(File(File("checkoutDir").absoluteFile, "workingDir"), VirtualFileSystemService.absolute(true)),
                        emptyList<String>()),

                // working dir is relative inside checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File(File("checkoutDir"), "workingDir"),
                        emptyList<File>(),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addDirectory(File(File("checkoutDir"), "workingDir")),
                        emptyList<String>()),

                // project outside checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File("checkoutDir").absoluteFile,
                        listOf(File("dir", "my.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addFile(File(File("dir"), "my.proj"), VirtualFileSystemService.absolute(true)),
                        listOf("The file(s) \"${File(File("dir"), "my.proj")}\" are located outside of the build checkout directory: \"${File("checkoutDir").absoluteFile}\". In this case there can be problems with running this build tests on TeamCity agent. Please refer to this issue for details: https://youtrack.jetbrains.com/issue/TW-52485")),

                // projects outside checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File("checkoutDir").absoluteFile,
                        listOf(File("dir", "my.proj"), File("dir2", "my2.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addFile(File(File("dir"), "my.proj"), VirtualFileSystemService.absolute(true))
                                .addFile(File(File("dir2"), "my2.proj"), VirtualFileSystemService.absolute(true)),
                        listOf("The file(s) \"${File(File("dir"), "my.proj")}, ${File(File("dir2"), "my2.proj")}\" are located outside of the build checkout directory: \"${File("checkoutDir").absoluteFile}\". In this case there can be problems with running this build tests on TeamCity agent. Please refer to this issue for details: https://youtrack.jetbrains.com/issue/TW-52485")),

                // project outside checkout dir and project inside checkout dir with related path
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File("checkoutDir").absoluteFile,
                        listOf(File("dir", "my.proj"), File("dir2", "my2.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addFile(File(File("dir"), "my.proj"), VirtualFileSystemService.absolute(true))
                                .addFile(File(File("dir2"), "my2.proj")),
                        listOf("The file(s) \"${File(File("dir"), "my.proj")}\" are located outside of the build checkout directory: \"${File("checkoutDir").absoluteFile}\". In this case there can be problems with running this build tests on TeamCity agent. Please refer to this issue for details: https://youtrack.jetbrains.com/issue/TW-52485")),

                // project outside checkout dir and project inside checkout dir with absolute path
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File("checkoutDir").absoluteFile,
                        listOf(File("dir", "my.proj"), File(File(File("checkoutDir").absoluteFile, "dir2"), "my2.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addFile(File(File("dir"), "my.proj"), VirtualFileSystemService.absolute(true))
                                .addFile(File(File(File("checkoutDir").absoluteFile, "dir2"), "my2.proj"), VirtualFileSystemService.absolute(true)),
                        listOf("The file(s) \"${File(File("dir"), "my.proj")}\" are located outside of the build checkout directory: \"${File("checkoutDir").absoluteFile}\". In this case there can be problems with running this build tests on TeamCity agent. Please refer to this issue for details: https://youtrack.jetbrains.com/issue/TW-52485")),

                // project inside checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        File("checkoutDir").absoluteFile,
                        listOf(File("dir", "my.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addFile(File(File(File("checkoutDir").absoluteFile, "dir"), "my.proj")),
                        emptyList<String>()))
    }

    @Test(dataProvider = "testData")
    fun shouldCopyLoggerAndCreateReadme(
            checkoutDirectory: File,
            workingDirectory: File,
            targetFiles: List<File>,
            fileSystemService: VirtualFileSystemService,
            expectedMessages: List<String>) {
        // Given
        val environmentAnalyzer = VSTestLoggerEnvironmentAnalyzerImpl(
                _pathService,
                fileSystemService,
                _loggerService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDirectory))

                allowing<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                // Then
                for (expectedMessage in expectedMessages) {
                    oneOf<LoggerService>(_loggerService).writeWarning(expectedMessage)
                }
            }
        })

        environmentAnalyzer.analyze(targetFiles)
        _ctx.assertIsSatisfied()
    }
}