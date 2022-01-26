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

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.util.*

class VSTestLoggerEnvironmentBuilderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _loggerResolver: LoggerResolver
    private lateinit var _fileSystemService: FileSystemService
    private lateinit var _loggerService: LoggerService
    private lateinit var _environmentCleaner: EnvironmentCleaner
    private lateinit var _environmentAnalyzer: VSTestLoggerEnvironmentAnalyzer
    private lateinit var _testReportingParameters: TestReportingParameters
    private lateinit var _dotnetCommand: DotnetCommand

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock(PathsService::class.java)
        _fileSystemService = _ctx.mock(FileSystemService::class.java)
        _loggerResolver = _ctx.mock(LoggerResolver::class.java)
        _loggerService = _ctx.mock(LoggerService::class.java)
        _environmentCleaner = _ctx.mock(EnvironmentCleaner::class.java)
        _environmentAnalyzer = _ctx.mock(VSTestLoggerEnvironmentAnalyzer::class.java)
        _testReportingParameters = _ctx.mock(TestReportingParameters::class.java)
        _dotnetCommand = _ctx.mock(DotnetCommand::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // one project in checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        sequenceOf(TargetArguments(sequenceOf(CommandLineArgument(File("dir", "my.proj").path)))),
                        listOf(File("dir", "my.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true))
                                .addFile(File(File(File("checkoutDir").absoluteFile, "dir"), "my.proj")),
                        listOf(
                                File(File("checkoutDir").absoluteFile, "${VSTestLoggerEnvironmentBuilder.directoryPrefix}abc"),
                                File(File(File("checkoutDir").absoluteFile, "${VSTestLoggerEnvironmentBuilder.directoryPrefix}abc"), VSTestLoggerEnvironmentBuilder.readmeFileName))))
    }

    @Test(dataProvider = "testData")
    fun shouldCopyLoggerAndCreateReadme(
            checkoutDirectory: File,
            targetArguments: Sequence<TargetArguments>,
            targetFiles: List<File>,
            fileSystemService: VirtualFileSystemService,
            expectedDirs: List<File>) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), _dotnetCommand)
        val loggerFile = File("vstest15", "logger.dll")
        fileSystemService.addFile(loggerFile.absoluteFile)

        val uniqueName = "abc"
        val loggerEnvironment = VSTestLoggerEnvironmentBuilder(
                _pathService,
                fileSystemService,
                _loggerResolver,
                _loggerService,
                _testReportingParameters,
                _environmentCleaner,
                _environmentAnalyzer)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<TestReportingParameters>(_testReportingParameters).getMode(context)
                will(returnValue(EnumSet.of(TestReportingMode.On)))

                oneOf<DotnetCommand>(_dotnetCommand).targetArguments
                will(returnValue(targetArguments))

                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDirectory))

                oneOf<EnvironmentCleaner>(_environmentCleaner).clean()

                oneOf<VSTestLoggerEnvironmentAnalyzer>(_environmentAnalyzer).analyze(targetFiles)

                allowing<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName))
            }
        })

        val ticket = loggerEnvironment.build(context)

        // Then
        _ctx.assertIsSatisfied()
        for (expectedDir in expectedDirs) {
            Assert.assertEquals(fileSystemService.isExists(expectedDir), true)
        }

        ticket.dispose()
        for (expectedDir in expectedDirs) {
            val dir = File(expectedDir, uniqueName)
            Assert.assertEquals(fileSystemService.isExists(dir), false)
        }
    }

    @DataProvider
    fun testDataNotBuildEnv(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(EnumSet.of<TestReportingMode>(TestReportingMode.Off)),
                arrayOf(EnumSet.of<TestReportingMode>(TestReportingMode.MultiAdapterPath)),
                arrayOf(EnumSet.of<TestReportingMode>(TestReportingMode.MultiAdapterPath, TestReportingMode.Off)),
                arrayOf(EnumSet.of<TestReportingMode>(TestReportingMode.MultiAdapterPath, TestReportingMode.On)),
                arrayOf(EnumSet.of<TestReportingMode>(TestReportingMode.Off, TestReportingMode.On, TestReportingMode.MultiAdapterPath)))
    }

    @Test(dataProvider = "testDataNotBuildEnv")
    fun shouldNotBuildEnvWhenSpecificTestReportingMode(modes: EnumSet<TestReportingMode>) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), _dotnetCommand)
        val targetFiles = listOf(File("dir", "my.proj"))
        val loggerEnvironment = VSTestLoggerEnvironmentBuilder(
                _pathService,
                _fileSystemService,
                _loggerResolver,
                _loggerService,
                _testReportingParameters,
                _environmentCleaner,
                _environmentAnalyzer)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<TestReportingParameters>(_testReportingParameters).getMode(context)
                will(returnValue(modes))

                never<DotnetCommand>(_dotnetCommand).targetArguments

                never<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)

                never<PathsService>(_pathService).getPath(PathType.Checkout)

                never<EnvironmentCleaner>(_environmentCleaner).clean()

                never<VSTestLoggerEnvironmentAnalyzer>(_environmentAnalyzer).analyze(targetFiles)

                never<PathsService>(_pathService).uniqueName
            }
        })

        loggerEnvironment.build(context).dispose()

        // Then
        _ctx.assertIsSatisfied()
    }
}