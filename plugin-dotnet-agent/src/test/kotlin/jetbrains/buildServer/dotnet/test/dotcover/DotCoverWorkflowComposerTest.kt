package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.rx.Disposable
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverWorkflowComposerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _parametersService: ParametersService
    private lateinit var _dotCoverProjectSerializer: DotCoverProjectSerializer
    private lateinit var _loggerService: LoggerService
    private lateinit var _coverageFilterProvider: CoverageFilterProvider
    private lateinit var _targetRegistry: TargetRegistry
    private lateinit var _targetRegistrationToken: Disposable

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx.mock<ParametersService>(ParametersService::class.java)
        _dotCoverProjectSerializer = _ctx.mock<DotCoverProjectSerializer>(DotCoverProjectSerializer::class.java)
        _loggerService = _ctx.mock<LoggerService>(LoggerService::class.java)
        _coverageFilterProvider = _ctx.mock<CoverageFilterProvider>(CoverageFilterProvider::class.java)
        _targetRegistry = _ctx.mock(TargetRegistry::class.java)
        _targetRegistrationToken = _ctx.mock(Disposable::class.java)
    }

    @Test
    fun shouldBeProfilerOfCodeCoverage() {
        // Given
        val composer = createInstance(VirtualFileSystemService())

        // When

        // Then
        Assert.assertEquals(composer.target, TargetType.CodeCoverageProfiler)
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "dotCover", VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "dotCover", VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            coverageType: String?,
            dotCoverPath: String?,
            fileSystemService: FileSystemService) {
        // Given
        val dotCoverProjectUniqueName = "proj000"
        val dotCoverSnapshotUniqueName = "snapshot000"
        val executableFile = File("sdk", "dotnet.exe")
        val workingDirectory = File("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File(dotCoverPath, DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile
        val dotCoverProject = DotCoverProject(commandLine, File(dotCoverProjectUniqueName), File(dotCoverSnapshotUniqueName))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                dotCoverExecutableFile,
                                workingDirectory,
                                listOf(
                                        CommandLineArgument("cover"),
                                        CommandLineArgument(dotCoverProject.configFile.absolutePath),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars)))
        val composer = createInstance(fileSystemService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<TargetRegistry>(_targetRegistry).activeTargets
                will(returnValue(sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)
                will(returnValue(coverageType))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
                will(returnValue(dotCoverPath))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Quiet.id))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)
                will(returnValue(null))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)
                will(returnValue(null))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverProjectExtension)
                will(returnValue(File(dotCoverProjectUniqueName)))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension)
                will(returnValue(File(dotCoverSnapshotUniqueName)))

                fileSystemService.write(dotCoverProject.configFile)
                {
                    oneOf<DotCoverProjectSerializer>(_dotCoverProjectSerializer).serialize(dotCoverProject, it)
                }

                oneOf<LoggerService>(_loggerService).writeMessage(DotCoverServiceMessage(File(dotCoverPath).absoluteFile))
                oneOf<LoggerService>(_loggerService).writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))

                oneOf<TargetRegistry>(_targetRegistry).register(TargetType.CodeCoverageProfiler)
                will(returnValue(_targetRegistrationToken))

                oneOf<Disposable>(_targetRegistrationToken).dispose()
            }
        })

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @DataProvider(name = "notComposeCases")
    fun getNotComposeCases(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf("other", "dotCover" as Any?),
                arrayOf("", "dotCover" as Any?),
                arrayOf("   ", "dotCover" as Any?),
                arrayOf(null, "dotCover" as Any?),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, null as Any?),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "" as Any?),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "   " as Any?))
    }

    @Test(dataProvider = "notComposeCases")
    fun shouldReturnsBaseWorkflowWhenCoverageDisabled(
            coverageType: String?,
            dotCoverPath: String?) {
        // Given
        val executableFile = File("sdk", "dotnet.exe")
        val workingDirectory = File("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)

        val composer = createInstance(VirtualFileSystemService())
        val baseWorkflow = Workflow(sequenceOf(commandLine))

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)
                will(returnValue(coverageType))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled")
                will(returnValue(null))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
                will(returnValue(dotCoverPath))
            }
        })

        val actualWorkflow = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), baseWorkflow)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualWorkflow, baseWorkflow)
    }

    @DataProvider(name = "showDiagnosticCases")
    fun getShowDiagnosticCases(): Array<Array<Verbosity>> {
        return arrayOf(
                arrayOf(Verbosity.Detailed),
                arrayOf(Verbosity.Diagnostic))
    }

    @Test(dataProvider = "showDiagnosticCases")
    fun shouldShowDiagnostic(verbosity: Verbosity) {
        // Given
        val dotCoverProjectUniqueName = "proj000"
        val dotCoverSnapshotUniqueName = "snapshot000"
        val executableFile = File("sdk", "dotnet.exe")
        val workingDirectory = File("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.CodeCoverageProfiler,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile
        val dotCoverProject = DotCoverProject(commandLine, File(dotCoverProjectUniqueName), File(dotCoverSnapshotUniqueName))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                dotCoverExecutableFile,
                                workingDirectory,
                                listOf(
                                        CommandLineArgument("cover"),
                                        CommandLineArgument(dotCoverProject.configFile.absolutePath),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars)))
        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)
        val composer = createInstance(fileSystemService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<TargetRegistry>(_targetRegistry).activeTargets
                will(returnValue(sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)
                will(returnValue(CoverageConstants.PARAM_DOTCOVER))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
                will(returnValue("dotCover"))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(verbosity.id))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)
                will(returnValue(null))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)
                will(returnValue(null))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverProjectExtension)
                will(returnValue(File(dotCoverProjectUniqueName)))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension)
                will(returnValue(File(dotCoverSnapshotUniqueName)))

                fileSystemService.write(dotCoverProject.configFile)
                {
                    oneOf<DotCoverProjectSerializer>(_dotCoverProjectSerializer).serialize(dotCoverProject, it)
                }

                // Check diagnostics info
                oneOf<LoggerService>(_loggerService).writeBlock("dotCover Settings")
                oneOf<LoggerService>(_loggerService).writeStandardOutput("Command line:")
                oneOf<LoggerService>(_loggerService).writeStandardOutput("  \"${File("sdk", "dotnet.exe").path}\" arg1", Color.Details)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("Filters:")
                val filter1 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "abc")
                val filter2 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "qwerty")
                oneOf<CoverageFilterProvider>(_coverageFilterProvider).filters
                will(returnValue(sequenceOf(filter1, filter2)))
                oneOf<LoggerService>(_loggerService).writeStandardOutput("  $filter1", Color.Details)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("  $filter2", Color.Details)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("Attribute Filters:")
                val attributeFilter = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "xyz")
                oneOf<CoverageFilterProvider>(_coverageFilterProvider).attributeFilters
                will(returnValue(sequenceOf(attributeFilter)))
                oneOf<LoggerService>(_loggerService).writeStandardOutput("  $attributeFilter", Color.Details)

                oneOf<LoggerService>(_loggerService).writeMessage(DotCoverServiceMessage(File("dotCover").absoluteFile))
                oneOf<LoggerService>(_loggerService).writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))

                oneOf<TargetRegistry>(_targetRegistry).register(TargetType.CodeCoverageProfiler)
                will(returnValue(_targetRegistrationToken))

                oneOf<Disposable>(_targetRegistrationToken).dispose()
            }
        })

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldNotPublishServiceMessageWhenWorkflowFailed() {
        // Given
        val dotCoverProjectUniqueName = "proj000"
        val dotCoverSnapshotUniqueName = "snapshot000"
        val executableFile = File("sdk", "dotnet.exe")
        val workingDirectory = File("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.CodeCoverageProfiler,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile
        val dotCoverProject = DotCoverProject(commandLine, File(dotCoverProjectUniqueName), File(dotCoverSnapshotUniqueName))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                dotCoverExecutableFile,
                                workingDirectory,
                                listOf(
                                        CommandLineArgument("cover"),
                                        CommandLineArgument(dotCoverProject.configFile.absolutePath),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars)))
        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)
        val composer = createInstance(fileSystemService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                allowing<TargetRegistry>(_targetRegistry).activeTargets
                will(returnValue(sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)
                will(returnValue(CoverageConstants.PARAM_DOTCOVER))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
                will(returnValue("dotCover"))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Normal.id))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)
                will(returnValue(null))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)
                will(returnValue(null))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverProjectExtension)
                will(returnValue(File(dotCoverProjectUniqueName)))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension)
                will(returnValue(File(dotCoverSnapshotUniqueName)))

                fileSystemService.write(dotCoverProject.configFile)
                {
                    oneOf<DotCoverProjectSerializer>(_dotCoverProjectSerializer).serialize(dotCoverProject, it)
                }

                never<LoggerService>(_loggerService).writeMessage(DotCoverServiceMessage(File("dotCover").absoluteFile))
                never<LoggerService>(_loggerService).writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))

                oneOf<TargetRegistry>(_targetRegistry).register(TargetType.CodeCoverageProfiler)
                will(returnValue(_targetRegistrationToken))

                oneOf<Disposable>(_targetRegistrationToken).dispose()
            }
        })

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Failed, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldTakeInAccountDotCoverArguments() {
        // Given
        val dotCoverProjectUniqueName = "proj000"
        val dotCoverSnapshotUniqueName = "snapshot000"
        val executableFile = File("sdk", "dotnet.exe")
        val workingDirectory = File("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile
        val dotCoverProject = DotCoverProject(commandLine, File(dotCoverProjectUniqueName), File(dotCoverSnapshotUniqueName))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                dotCoverExecutableFile,
                                workingDirectory,
                                listOf(
                                        CommandLineArgument("cover"),
                                        CommandLineArgument(dotCoverProject.configFile.absolutePath),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false"),
                                        CommandLineArgument("/ProcessFilters=-:sqlservr.exe"),
                                        CommandLineArgument("/arg")
                                ),
                                envVars)))

        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)
        val composer = createInstance(fileSystemService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<TargetRegistry>(_targetRegistry).activeTargets
                will(returnValue(sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)
                will(returnValue(CoverageConstants.PARAM_DOTCOVER))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
                will(returnValue(dotCoverExecutableFile.parentFile.absolutePath))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Normal.id))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)
                will(returnValue("/ProcessFilters=-:sqlservr.exe /arg"))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)
                will(returnValue(null))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverProjectExtension)
                will(returnValue(File(dotCoverProjectUniqueName)))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension)
                will(returnValue(File(dotCoverSnapshotUniqueName)))

                fileSystemService.write(dotCoverProject.configFile)
                {
                    oneOf<DotCoverProjectSerializer>(_dotCoverProjectSerializer).serialize(dotCoverProject, it)
                }

                oneOf<LoggerService>(_loggerService).writeMessage(DotCoverServiceMessage(dotCoverExecutableFile.parentFile))
                oneOf<LoggerService>(_loggerService).writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))

                oneOf<TargetRegistry>(_targetRegistry).register(TargetType.CodeCoverageProfiler)
                will(returnValue(_targetRegistrationToken))

                oneOf<Disposable>(_targetRegistrationToken).dispose()
            }
        })

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldSupportLogFileConfigParamArguments() {
        // Given
        val dotCoverProjectUniqueName = "proj000"
        val dotCoverSnapshotUniqueName = "snapshot000"
        val executableFile = File("sdk", "dotnet.exe")
        val workingDirectory = File("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile
        val dotCoverProject = DotCoverProject(commandLine, File(dotCoverProjectUniqueName), File(dotCoverSnapshotUniqueName))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                dotCoverExecutableFile,
                                workingDirectory,
                                listOf(
                                        CommandLineArgument("cover"),
                                        CommandLineArgument(dotCoverProject.configFile.absolutePath),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false"),
                                        CommandLineArgument("/LogFile=${File("logPath", "dotCover99.log")}"),
                                        CommandLineArgument("/ProcessFilters=-:sqlservr.exe"),
                                        CommandLineArgument("/arg")
                                ),
                                envVars)))

        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)
        val composer = createInstance(fileSystemService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<TargetRegistry>(_targetRegistry).activeTargets
                will(returnValue(sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)
                will(returnValue(CoverageConstants.PARAM_DOTCOVER))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
                will(returnValue(dotCoverExecutableFile.parentFile.absolutePath))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Normal.id))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)
                will(returnValue("/ProcessFilters=-:sqlservr.exe /arg"))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)
                will(returnValue("logPath"))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverProjectExtension)
                will(returnValue(File(dotCoverProjectUniqueName)))

                oneOf<PathsService>(_pathService).getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension)
                will(returnValue(File(dotCoverSnapshotUniqueName)))

                fileSystemService.write(dotCoverProject.configFile)
                {
                    oneOf<DotCoverProjectSerializer>(_dotCoverProjectSerializer).serialize(dotCoverProject, it)
                }

                oneOf<LoggerService>(_loggerService).writeMessage(DotCoverServiceMessage(dotCoverExecutableFile.parentFile))
                oneOf<LoggerService>(_loggerService).writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))

                oneOf<TargetRegistry>(_targetRegistry).register(TargetType.CodeCoverageProfiler)
                will(returnValue(_targetRegistrationToken))

                oneOf<Disposable>(_targetRegistrationToken).dispose()
            }
        })

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createInstance(fileSystemService: FileSystemService): WorkflowComposer {
        return DotCoverWorkflowComposer(
                _pathService,
                _parametersService,
                fileSystemService,
                _dotCoverProjectSerializer,
                _loggerService,
                ArgumentsServiceStub(),
                _coverageFilterProvider,
                _targetRegistry)
    }
}