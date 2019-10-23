package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.*
import io.mockk.impl.annotations.MockK
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
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverWorkflowComposerTest {
    @MockK private lateinit var _pathService: PathsService
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _dotCoverProjectSerializer: DotCoverProjectSerializer
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _coverageFilterProvider: CoverageFilterProvider
    @MockK private lateinit var _targetRegistry: TargetRegistry
    @MockK private lateinit var _targetRegistrationToken: Disposable
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
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
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "dotCover", VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))),
                arrayOf(CoverageConstants.PARAM_DOTCOVER, "dotCover", VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            coverageType: String?,
            dotCoverPath: String?,
            fileSystemService: FileSystemService) {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path(File("wd").path)
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File(dotCoverPath, "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars)))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _targetRegistry.activeTargets } returns sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns coverageType
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverPath
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Quiet.id
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path(dotCoverPath!!))) } returns Unit
        every { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, Path("v_snap"))) } returns Unit
        every { _targetRegistry.register(TargetType.CodeCoverageProfiler) } returns _targetRegistrationToken
        every { _targetRegistrationToken.dispose() } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        verify { _targetRegistrationToken.dispose() }
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
    fun shouldReturnBaseWorkflowWhenCoverageDisabled(
            coverageType: String?,
            dotCoverPath: String?) {
        // Given
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
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
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns coverageType
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverPath
        every { _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled") } returns null

        val actualWorkflow = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), baseWorkflow).commandLines.toList()

        // Then
        Assert.assertEquals(actualWorkflow, baseWorkflow.commandLines.toList())
    }

    @Test
    fun shouldNotWrapNonToolTargetsByDotCover() {
        // Given
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)

        val composer = createInstance(VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe")))
        val baseWorkflow = Workflow(sequenceOf(commandLine))

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _targetRegistry.activeTargets } returns sequenceOf(TargetType.MemoryProfiler, TargetType.SystemDiagnostics)
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled") } returns null
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Quiet.id
        every { _targetRegistrationToken.dispose() } returns Unit
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        val actualWorkflow = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), baseWorkflow).commandLines.toList()

        // Then
        Assert.assertEquals(actualWorkflow, baseWorkflow.commandLines.toList())
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
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars)))
        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _targetRegistry.activeTargets } returns sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns verbosity.id
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, Path(dotCoverSnapshotUniqueName.path))) } returns Unit
        every { _targetRegistry.register(TargetType.CodeCoverageProfiler) } returns _targetRegistrationToken
        every { _targetRegistrationToken.dispose() } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        val blockToken = mockk<Disposable>()
        every { _loggerService.writeBlock("dotCover Settings") } returns blockToken
        every { _loggerService.writeStandardOutput("Command line:") } returns Unit
        every { _loggerService.writeStandardOutput("  \"${File("dotnet", "dotnet.exe").path}\" arg1", Color.Details) } returns Unit
        every { _loggerService.writeStandardOutput("Filters:") } returns Unit
        val filter1 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "abc")
        val filter2 = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, CoverageFilter.Any, "qwerty")
        every { _coverageFilterProvider.filters } returns sequenceOf(filter1, filter2)
        every { _loggerService.writeStandardOutput("  $filter1", Color.Details) } returns Unit
        every { _loggerService.writeStandardOutput("  $filter2", Color.Details) } returns Unit
        every { _loggerService.writeStandardOutput("Attribute Filters:") } returns Unit
        val attributeFilter = CoverageFilter(CoverageFilter.CoverageFilterType.Exclude, CoverageFilter.Any, "xyz")
        every { _coverageFilterProvider.attributeFilters } returns sequenceOf(attributeFilter)
        every { _loggerService.writeStandardOutput("  $attributeFilter", Color.Details) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, Path("v_snap"))) } returns Unit

        every { blockToken.dispose() } returns Unit

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        verify { blockToken.dispose() }
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldNotPublishServiceMessageWhenWorkflowFailed() {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1", CommandLineArgumentType.Secondary))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false")
                                ),
                                envVars)))
        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _targetRegistry.activeTargets } returns sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Normal.id
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _targetRegistry.register(TargetType.CodeCoverageProfiler) } returns _targetRegistrationToken
        every { _targetRegistrationToken.dispose() } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Failed, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        verify { _targetRegistrationToken.dispose() }
        verify(exactly = 0) { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) }
        verify(exactly = 0) { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, Path(dotCoverSnapshotUniqueName.path))) }

        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldTakeInAccountDotCoverArguments() {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false"),
                                        CommandLineArgument("/ProcessFilters=-:sqlservr.exe", CommandLineArgumentType.Custom),
                                        CommandLineArgument("/arg", CommandLineArgumentType.Custom)
                                ),
                                envVars)))

        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _targetRegistry.activeTargets } returns sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Normal.id
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns "/ProcessFilters=-:sqlservr.exe /arg"
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, Path("v_snap"))) } returns Unit
        every { _targetRegistry.register(TargetType.CodeCoverageProfiler) } returns _targetRegistrationToken
        every { _targetRegistrationToken.dispose() } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldSupportLogFileConfigParamArguments() {
        // Given
        val dotCoverProjectUniqueName = Path("proj000")
        val dotCoverSnapshotUniqueName = Path("snapshot000")
        val executableFile = Path(File("dotnet", "dotnet.exe").path)
        val workingDirectory = Path("wd")
        val args = listOf(CommandLineArgument("arg1"))
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
        val commandLine = CommandLine(
                TargetType.Tool,
                executableFile,
                workingDirectory,
                args,
                envVars)
        val dotCoverExecutableFile = File("dotCover", "dotCover.exe")
        val dotCoverProject = DotCoverProject(
                CommandLine(
                        TargetType.Tool,
                        executableFile,
                        Path("v_wd"),
                        args,
                        envVars),
                Path("v_proj"),
                Path ("v_snap"))
        val expectedWorkflow = Workflow(
                sequenceOf(
                        CommandLine(
                                TargetType.CodeCoverageProfiler,
                                Path("v_dotCover"),
                                Path("wd"),
                                listOf(
                                        CommandLineArgument("cover", CommandLineArgumentType.Mandatory),
                                        CommandLineArgument("v_proj", CommandLineArgumentType.Target),
                                        CommandLineArgument("/ReturnTargetExitCode"),
                                        CommandLineArgument("/NoCheckForUpdates"),
                                        CommandLineArgument("/AnalyzeTargetArguments=false"),
                                        CommandLineArgument("/LogFile=v_log", CommandLineArgumentType.Infrastructural)
                                ),
                                envVars)))

        val fileSystemService = VirtualFileSystemService().addFile(File("dotCover", "dotCover.exe"))
        val composer = createInstance(fileSystemService)

        // When
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _targetRegistry.activeTargets } returns sequenceOf(TargetType.MemoryProfiler, TargetType.Tool)
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE) } returns CoverageConstants.PARAM_DOTCOVER
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "dotCover"
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Normal.id
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns "logPath"
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverConfigExtension) } returns File(dotCoverProjectUniqueName.path)
        every { _pathService.getTempFileName(DotCoverWorkflowComposer.DotCoverSnapshotExtension) } returns File(dotCoverSnapshotUniqueName.path)
        every { _dotCoverProjectSerializer.serialize(dotCoverProject, any()) } returns Unit
        every { _loggerService.writeMessage(DotCoverServiceMessage(Path("dotCover"))) } returns Unit
        every { _loggerService.writeMessage(ImportDataServiceMessage(DotCoverWorkflowComposer.DotCoverToolName, Path("v_snap"))) } returns Unit
        every { _targetRegistry.register(TargetType.CodeCoverageProfiler) } returns _targetRegistrationToken
        every { _targetRegistrationToken.dispose() } returns Unit
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns "v_dotCover"
        every { _virtualContext.resolvePath(dotCoverProjectUniqueName.path) } returns "v_proj"
        every { _virtualContext.resolvePath(dotCoverSnapshotUniqueName.path) } returns "v_snap"
        every { _virtualContext.resolvePath(File("logPath", "dotCover99.log").canonicalPath) } returns "v_log"
        every { _virtualContext.resolvePath("wd") } returns "v_wd"

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Workflow(sequenceOf(commandLine))).commandLines.toList()

        // Then
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
                _targetRegistry,
                _virtualContext)
    }
}