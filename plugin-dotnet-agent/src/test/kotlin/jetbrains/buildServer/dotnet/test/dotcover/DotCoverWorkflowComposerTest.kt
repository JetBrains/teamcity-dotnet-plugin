package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.*
import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverWorkflowComposerTest {
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _parametersService: ParametersService? = null
    private var _dotCoverProjectSerializer: DotCoverProjectSerializer? = null
    private var _loggerService: LoggerService? = null
    private var _workflowContext: WorkflowContext? = null
    private var _coverageFilterProvider: CoverageFilterProvider? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx!!.mock<ParametersService>(ParametersService::class.java)
        _dotCoverProjectSerializer = _ctx!!.mock<DotCoverProjectSerializer>(DotCoverProjectSerializer::class.java)
        _loggerService = _ctx!!.mock<LoggerService>(LoggerService::class.java)
        _workflowContext = _ctx!!.mock<WorkflowContext>(WorkflowContext::class.java)
        _coverageFilterProvider = _ctx!!.mock<CoverageFilterProvider>(CoverageFilterProvider::class.java)
    }

    fun shouldBeProfilerOfCodeCoverage() {
        // Given
        val composer = createInstance(VirtualFileSystemService())

        // When

        // Then
        Assert.assertEquals(composer.target, TargetType.ProfilerOfCodeCoverage)
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("true", "dotCover", VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)),
                arrayOf("True", "dotCover", VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            paramEnabled: String?,
            dotCoverPath: String?,
            fileSystemService: FileSystemService) {
        // Given
        val dotCoverProjectUniqueName = "proj000"
        val dotCoverSnapshotUniqueName = "snapshot000"
        val tempDirectory = File("temp")
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
        val dotCoverExecutableFile = File(dotCoverPath, DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile;
        val dotCoverProject = DotCoverProject(
                commandLine,
                File(tempDirectory, dotCoverProjectUniqueName + DotCoverWorkflowComposer.DotCoverProjectExtension),
                File(tempDirectory, dotCoverSnapshotUniqueName + DotCoverWorkflowComposer.DotCoverSnapshotExtension))

        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.Tool,
                                dotCoverExecutableFile,
                                workingDirectory,
                                listOf(
                                        CommandLineArgument("cover"),
                                        CommandLineArgument(dotCoverProject.configFile.absolutePath),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates")
                                ),
                                envVars)))
        val composer = createInstance(fileSystemService)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_ENABLED)
                will(returnValue(paramEnabled))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_HOME)
                will(returnValue(dotCoverPath))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Quiet.id))

                oneOf<PathsService>(_pathService).getPath(PathType.BuildTemp)
                will(returnValue(tempDirectory))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(dotCoverProjectUniqueName))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(dotCoverSnapshotUniqueName))

                fileSystemService.write(dotCoverProject.configFile)
                {
                    oneOf<DotCoverProjectSerializer>(_dotCoverProjectSerializer).serialize(dotCoverProject, it)
                }

                oneOf<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(CommandLineResult(sequenceOf(0), emptySequence(), emptySequence())))

                oneOf<LoggerService>(_loggerService).onMessage(DotCoverServiceMessage(File(dotCoverPath).absoluteFile))
                oneOf<LoggerService>(_loggerService).onMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))
            }
        })

        val actualCommandLines = composer.compose(_workflowContext!!, Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @DataProvider(name = "notComposeCases")
    fun getNotComposeCases(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf("false", "dotCover" as Any?),
                arrayOf("", "dotCover" as Any?),
                arrayOf("   ", "dotCover" as Any?),
                arrayOf(null, "dotCover" as Any?),
                arrayOf("true", null as Any?),
                arrayOf("true", "" as Any?),
                arrayOf("true", "   " as Any?))
    }

    @Test(dataProvider = "notComposeCases")
    fun shouldReturnsBaseWorkflowWhenCoverageDisabled(
            paramEnabled: String?,
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
        val baseWorkflow = Workflow(sequenceOf(commandLine));

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_ENABLED)
                will(returnValue(paramEnabled))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_HOME)
                will(returnValue(dotCoverPath))
            }
        })

        val actualWorkflow = composer.compose(_workflowContext!!, baseWorkflow)

        // Then
        _ctx!!.assertIsSatisfied()
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
        val tempDirectory = File("temp")
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
        val dotCoverExecutableFile = File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile;
        val dotCoverProject = DotCoverProject(
                commandLine,
                File(tempDirectory, dotCoverProjectUniqueName + DotCoverWorkflowComposer.DotCoverProjectExtension),
                File(tempDirectory, dotCoverSnapshotUniqueName + DotCoverWorkflowComposer.DotCoverSnapshotExtension))

        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.Tool,
                                dotCoverExecutableFile,
                                workingDirectory,
                                listOf(
                                        CommandLineArgument("cover"),
                                        CommandLineArgument(dotCoverProject.configFile.absolutePath),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates")
                                ),
                                envVars)))
        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", DotCoverWorkflowComposer.DotCoverExecutableFile).absoluteFile)
        val composer = createInstance(fileSystemService)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_ENABLED)
                will(returnValue("true"))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_HOME)
                will(returnValue("dotCover"))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(verbosity.id))

                oneOf<PathsService>(_pathService).getPath(PathType.BuildTemp)
                will(returnValue(tempDirectory))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(dotCoverProjectUniqueName))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(dotCoverSnapshotUniqueName))

                fileSystemService.write(dotCoverProject.configFile)
                {
                    oneOf<DotCoverProjectSerializer>(_dotCoverProjectSerializer).serialize(dotCoverProject, it)
                }

                oneOf<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(CommandLineResult(sequenceOf(0), emptySequence(), emptySequence())))

                // Check diagnostics info
                oneOf<LoggerService>(_loggerService).onBlock("dotCover Settings")
                oneOf<LoggerService>(_loggerService).onStandardOutput("Command line:")
                oneOf<LoggerService>(_loggerService).onStandardOutput("  \"${File("sdk", "dotnet.exe").path}\" arg1", Color.Details)
                oneOf<LoggerService>(_loggerService).onStandardOutput("Filters:")
                val filter1 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "abc")
                val filter2 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "qwerty")
                oneOf<CoverageFilterProvider>(_coverageFilterProvider).filters
                will(returnValue(sequenceOf(filter1, filter2)))
                oneOf<LoggerService>(_loggerService).onStandardOutput("  ${filter1}", Color.Details)
                oneOf<LoggerService>(_loggerService).onStandardOutput("  ${filter2}", Color.Details)
                oneOf<LoggerService>(_loggerService).onStandardOutput("Attribute Filters:")
                val attributeFilter = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "xyz")
                oneOf<CoverageFilterProvider>(_coverageFilterProvider).attributeFilters
                will(returnValue(sequenceOf(attributeFilter)))
                oneOf<LoggerService>(_loggerService).onStandardOutput("  ${attributeFilter}", Color.Details)

                oneOf<LoggerService>(_loggerService).onMessage(DotCoverServiceMessage(File("dotCover").absoluteFile))
                oneOf<LoggerService>(_loggerService).onMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))
            }
        })

        val actualCommandLines = composer.compose(_workflowContext!!, Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createInstance(fileSystemService: FileSystemService): WorkflowComposer {
        return DotCoverWorkflowComposer(
                _pathService!!,
                _parametersService!!,
                fileSystemService,
                _dotCoverProjectSerializer!!,
                _loggerService!!,
                ArgumentsServiceStub(),
                _coverageFilterProvider!!)
    }
}