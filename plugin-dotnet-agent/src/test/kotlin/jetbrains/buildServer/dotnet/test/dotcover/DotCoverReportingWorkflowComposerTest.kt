package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.TempFiles
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.*
import jetbrains.buildServer.dotcover.DotCoverProject.MergeCommandData
import jetbrains.buildServer.dotcover.DotCoverProject.ReportCommandData
import jetbrains.buildServer.dotcover.DotCoverReportingWorkflowComposer.Companion.DOTCOVER_CONFIG_EXTENSION
import jetbrains.buildServer.dotcover.command.DotCoverCommandType
import jetbrains.buildServer.dotcover.command.DotCoverCoverCommandLineBuilder
import jetbrains.buildServer.dotcover.command.DotCoverMergeCommandLineBuilder
import jetbrains.buildServer.dotcover.command.DotCoverReportCommandLineBuilder
import jetbrains.buildServer.dotcover.report.DotCoverTeamCityReportGenerator
import jetbrains.buildServer.dotcover.statistics.DotnetCoverageStatisticsPublisher
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotcover.report.artifacts.ArtifactsUploader
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.mono.MonoToolProvider
import jetbrains.buildServer.rx.Disposable
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverReportingWorkflowComposerTest {
    @MockK private lateinit var _pathService: PathsService
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _dotCoverProjectSerializer: DotCoverProjectSerializer
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _environmentVariables: EnvironmentVariables
    @MockK private lateinit var _entryPointSelector: DotCoverEntryPointSelector
    @MockK private lateinit var _dotCoverSettings: DotCoverSettings
    @MockK private lateinit var _dotCoverTeamCityReportGenerator: DotCoverTeamCityReportGenerator
    @MockK private lateinit var _dotnetCoverageStatisticsPublisher: DotnetCoverageStatisticsPublisher
    @MockK private lateinit var _uploader: ArtifactsUploader
    @MockK private lateinit var _monoToolProvider: MonoToolProvider
    @MockK private lateinit var _buildStepContext: BuildStepContext
    @MockK private lateinit var _blockToken: Disposable

    private val _defaultVariables = sequenceOf(CommandLineEnvironmentVariable("Abc", "C"))
    private val tempFiles = TempFiles()
    private val dotCoverPath = "dotCover"
    private val dotCoverExecutableFile = File(dotCoverPath, "dotCover.exe")
    private val dotCoverExecutableVirtualPath = Path("v_dotCover")
    private lateinit var agentTmp: File
    private lateinit var workingDir: File
    private lateinit var virtualWorkingDir: File
    private lateinit var checkoutDir: File
    private lateinit var virtualCheckoutDir: File
    private lateinit var fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        fileSystemService = FileSystemServiceImpl()
        val testTempDirectory = tempFiles.createTempDir()

        agentTmp = File(testTempDirectory, "agentTmp")
        agentTmp.mkdirs()
        workingDir = File(testTempDirectory, "workingDir")
        virtualWorkingDir = File(workingDir, "v_workingDir")
        virtualWorkingDir.mkdirs()
        checkoutDir = File(testTempDirectory, "checkoutDir")
        virtualCheckoutDir = File(checkoutDir, "v_checkoutDir")
        virtualCheckoutDir.mkdirs()
        every { _pathService.getPath(PathType.AgentTemp) } returns agentTmp
        every { _pathService.getPath(PathType.WorkingDirectory) } returns workingDir
        every { _virtualContext.resolvePath(workingDir.absolutePath) } returns virtualWorkingDir.absolutePath
        every { _pathService.getPath(PathType.Checkout) } returns checkoutDir
        every { _virtualContext.resolvePath(checkoutDir.absolutePath) } returns virtualCheckoutDir.absolutePath
        every { _blockToken.dispose() } returns Unit
        every { _loggerService.writeTraceBlock(any()) } returns _blockToken
        every { _loggerService.writeTrace(any()) } returns Unit
        every { _loggerService.writeDebug(any()) } returns Unit
        every { _loggerService.writeWarning(any()) } returns Unit
    }

    @Test
    fun `should return an empty sequence when dotCover home is not set`() {
        // Arrange
        val composer = createInstance(fileSystemService)
        every { _dotCoverSettings.dotCoverMode } returns DotCoverMode.Wrapper
        every { _dotCoverSettings.dotCoverHomePath } returns ""

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertTrue(actualCommandLines.isEmpty())
        verify { _loggerService.writeWarning("Skip code coverage: dotCover is enabled however tool home path has not been set") }
    }

    @Test
    fun `should return an empty sequence when dotCover mode is Disabled`() {
        // Arrange
        val composer = createInstance(fileSystemService)
        every { _dotCoverSettings.dotCoverMode } returns DotCoverMode.Disabled

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertTrue(actualCommandLines.isEmpty())
    }

    @Test
    fun `should return an empty sequence when merge and report are disabled`() {
        // Arrange
        every { _dotCoverSettings.dotCoverMode } returns DotCoverMode.Wrapper
        every { _dotCoverSettings.dotCoverHomePath } returns "dotCover"
        every { _entryPointSelector.select() } answers { Result.success(File(dotCoverExecutableFile.path)) }
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns dotCoverExecutableVirtualPath.path
        every { _dotCoverSettings.shouldMergeSnapshots() } returns Pair(false, "Merging dotCover snapshots is disabled; skipping this stage")
        every { _dotCoverSettings.shouldGenerateReport() } returns Pair(false, "Building a coverage report is disabled; skipping this stage")
        val composer = createInstance(fileSystemService)

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertTrue(actualCommandLines.isEmpty())
        verify(exactly = 1) { _loggerService.writeDebug("Merging dotCover snapshots is disabled; skipping this stage") }
        verify(exactly = 1) { _loggerService.writeDebug("Building a coverage report is disabled; skipping this stage") }
    }

    @DataProvider(name = "DotCoverMode provider")
    fun `DotCoverMode provider`() = arrayOf(
        arrayOf(DotCoverMode.Wrapper),
        arrayOf(DotCoverMode.Runner),
    )

    @Test(dataProvider = "DotCoverMode provider")
    fun `should not merge when there are no snapshots`(dotCoverMode: DotCoverMode) {
        // Arrange
        every { _dotCoverSettings.dotCoverMode } returns dotCoverMode
        every { _dotCoverSettings.dotCoverHomePath } returns "dotCover"
        every { _entryPointSelector.select() } answers { Result.success(File(dotCoverExecutableFile.path)) }
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns dotCoverExecutableVirtualPath.path
        every { _dotCoverSettings.shouldMergeSnapshots() } returns Pair(true, "")
        every { _dotCoverSettings.shouldGenerateReport() } returns Pair(false, "")

        val buildStepId = "buildStep"
        val outputSnapshotFile = File(agentTmp, "outputSnapshot_$buildStepId.dcvr")
        every { _virtualContext.resolvePath(outputSnapshotFile.path) } returns outputSnapshotFile.path
        every { _dotCoverSettings.buildStepId } returns buildStepId
        every { _dotCoverSettings.additionalSnapshotPaths } returns emptySequence()

        val snapshots = createSnapshots(0)
        val dotCoverMergeProject = DotCoverProject(DotCoverCommandType.Merge, coverCommandData = null,
            MergeCommandData(sourceFiles = snapshots, outputFile = Path(outputSnapshotFile.path)))
        every { _dotCoverProjectSerializer.serialize(dotCoverMergeProject, any()) } returns Unit

        val composer = createInstance(fileSystemService)

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertTrue(actualCommandLines.isEmpty())
        verify(exactly = 1) { _loggerService.writeDebug("Snapshot files not found; skipping merge stage") }
    }

    @Test(dataProvider = "DotCoverMode provider")
    fun `should rename a snapshot when there is a single snapshot`(dotCoverMode: DotCoverMode) {
        // Arrange
        every { _dotCoverSettings.dotCoverMode } returns dotCoverMode
        every { _dotCoverSettings.dotCoverHomePath } returns "dotCover"
        every { _entryPointSelector.select() } answers { Result.success(File(dotCoverExecutableFile.path)) }
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns dotCoverExecutableVirtualPath.path
        every { _dotCoverSettings.shouldMergeSnapshots() } returns Pair(true, "")
        every { _dotCoverSettings.shouldGenerateReport() } returns Pair(false, "")

        val buildStepId = "buildStep"
        val outputSnapshotFile = File(agentTmp, "outputSnapshot_$buildStepId.dcvr")
        every { _virtualContext.resolvePath(outputSnapshotFile.path) } returns outputSnapshotFile.path
        every { _dotCoverSettings.buildStepId } returns buildStepId
        every { _dotCoverSettings.additionalSnapshotPaths } returns emptySequence()

        val snapshots = createSnapshots(1)
        val dotCoverMergeProject = DotCoverProject(DotCoverCommandType.Merge, coverCommandData = null,
            MergeCommandData(sourceFiles = snapshots, outputFile = Path(outputSnapshotFile.path)))
        every { _dotCoverProjectSerializer.serialize(dotCoverMergeProject, any()) } returns Unit

        val composer = createInstance(fileSystemService)

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertTrue(actualCommandLines.isEmpty())
        verify(exactly = 1) { _loggerService.writeDebug("""
                No need to execute merge command: there is a single snapshot file.
                Renaming it: from=${snapshots.first().path} to=${outputSnapshotFile.absolutePath}
            """.trimIndent()) }
        val actualSnapshots = agentTmp.listFiles()?.toList() ?: emptyList()
        Assert.assertTrue(actualSnapshots.size == 1)
        Assert.assertEquals(actualSnapshots.first().absolutePath, outputSnapshotFile.absolutePath)
    }

    @Test(dataProvider = "DotCoverMode provider")
    fun `should compose merge command when report is disabled and there are several snapshots`(dotCoverMode: DotCoverMode) {
        // Arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _dotCoverSettings.dotCoverMode } returns dotCoverMode
        every { _dotCoverSettings.dotCoverHomePath } returns "dotCover"
        every { _dotCoverSettings.coveragePostProcessingEnabled } returns false
        every { _entryPointSelector.select() } answers { Result.success(File(dotCoverExecutableFile.path)) }
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns dotCoverExecutableVirtualPath.path
        every { _dotCoverSettings.shouldMergeSnapshots() } returns Pair(true, "")
        every { _dotCoverSettings.shouldGenerateReport() } returns Pair(false, "")

        val buildStepId = "buildStep"
        val outputSnapshotFile = File(agentTmp, "outputSnapshot_$buildStepId.dcvr")
        val dotCoverMergeProjectFile = File(agentTmp,"merge_$DOTCOVER_CONFIG_EXTENSION")
        every { _virtualContext.resolvePath(outputSnapshotFile.path) } returns outputSnapshotFile.path
        every { _dotCoverSettings.buildStepId } returns buildStepId
        every { _dotCoverSettings.additionalSnapshotPaths } returns emptySequence()
        every { _pathService.getTempFileName("merge_${DOTCOVER_CONFIG_EXTENSION}") } returns dotCoverMergeProjectFile
        every { _virtualContext.resolvePath(dotCoverMergeProjectFile.path) } returns dotCoverMergeProjectFile.path

        createSnapshots(2)
        every { _dotCoverProjectSerializer.serialize(any(), any()) } returns Unit

        val expectedWorkflow = Workflow(
            sequenceOf(
                CommandLine(
                    baseCommandLine = null,
                    target = TargetType.CodeCoverageProfiler,
                    executableFile = dotCoverExecutableVirtualPath,
                    workingDirectory = Path(workingDir.absolutePath),
                    arguments = listOf(
                        CommandLineArgument("merge", CommandLineArgumentType.Mandatory),
                        CommandLineArgument(dotCoverMergeProjectFile.absolutePath, CommandLineArgumentType.Target)
                    ),
                    environmentVariables = _defaultVariables.toList(),
                    title = "dotCover merge")
            ))
        val composer = createInstance(fileSystemService)

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    @Test
    fun `should not compose report command when output snapshot is not found`() {
        // Arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _dotCoverSettings.dotCoverMode } returns DotCoverMode.Wrapper
        every { _dotCoverSettings.dotCoverHomePath } returns "dotCover"
        every { _dotCoverSettings.coveragePostProcessingEnabled } returns false
        every { _entryPointSelector.select() } answers { Result.success(File(dotCoverExecutableFile.path)) }
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns dotCoverExecutableVirtualPath.path
        every { _dotCoverSettings.shouldMergeSnapshots() } returns Pair(false, "")
        every { _dotCoverSettings.shouldGenerateReport() } returns Pair(true, "")

        val buildStepId = "buildStep"
        val dotCoverResultsDir = File(agentTmp, "dotCoverResults")
        val outputReportFile = File(dotCoverResultsDir, "CoverageReport_${buildStepId}.xml")
        every { _dotCoverSettings.buildStepId } returns buildStepId
        every { _virtualContext.resolvePath(dotCoverResultsDir.path) } returns dotCoverResultsDir.path
        every { _virtualContext.resolvePath(outputReportFile.path) } returns outputReportFile.path

        val composer = createInstance(fileSystemService)

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertTrue(actualCommandLines.isEmpty())
        verify(exactly = 1) { _loggerService.writeWarning("The dotCover report was not generated. Snapshot file not found") }
    }

    @Test
    fun `should not compose report command when output report file already exists`() {
        // Arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _dotCoverSettings.dotCoverMode } returns DotCoverMode.Wrapper
        every { _dotCoverSettings.dotCoverHomePath } returns "dotCover"
        every { _dotCoverSettings.coveragePostProcessingEnabled } returns false
        every { _entryPointSelector.select() } answers { Result.success(File(dotCoverExecutableFile.path)) }
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns dotCoverExecutableVirtualPath.path
        every { _dotCoverSettings.shouldMergeSnapshots() } returns Pair(false, "")
        every { _dotCoverSettings.shouldGenerateReport() } returns Pair(true, "")

        val buildStepId = "buildStep"
        val dotCoverResultsDir = File(agentTmp, "dotCoverResults")
        val outputReportFile = File(dotCoverResultsDir, "CoverageReport_${buildStepId}.xml")
        dotCoverResultsDir.mkdirs()
        outputReportFile.createNewFile()
        val outputSnapshotFile = File(agentTmp, "outputSnapshot_$buildStepId.dcvr")
        outputSnapshotFile.createNewFile()
        every { _dotCoverSettings.buildStepId } returns buildStepId
        every { _virtualContext.resolvePath(dotCoverResultsDir.path) } returns dotCoverResultsDir.path
        every { _virtualContext.resolvePath(outputReportFile.path) } returns outputReportFile.path

        val composer = createInstance(fileSystemService)

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertTrue(actualCommandLines.isEmpty())
        verify(exactly = 1) { _loggerService.writeDebug(
            "The report command has already been performed for this build step; outputReportFile=${outputReportFile.absolutePath}") }
    }

    @Test(dataProvider = "DotCoverMode provider")
    fun `should compose report command when merge is disabled`(dotCoverMode: DotCoverMode) {
        // Arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH) } returns null
        every { _environmentVariables.getVariables() } returns _defaultVariables
        every { _dotCoverSettings.dotCoverMode } returns dotCoverMode
        every { _dotCoverSettings.dotCoverHomePath } returns "dotCover"
        every { _dotCoverSettings.coveragePostProcessingEnabled } returns false
        every { _entryPointSelector.select() } answers { Result.success(File(dotCoverExecutableFile.path)) }
        every { _virtualContext.resolvePath(dotCoverExecutableFile.path) } returns dotCoverExecutableVirtualPath.path
        every { _dotCoverSettings.shouldMergeSnapshots() } returns Pair(false, "")
        every { _dotCoverSettings.shouldGenerateReport() } returns Pair(true, "")

        val buildStepId = "buildStep"
        val outputSnapshotFile = File(agentTmp, "outputSnapshot_$buildStepId.dcvr")
        outputSnapshotFile.createNewFile()
        val dotCoverResultsDir = File(agentTmp, "dotCoverResults")
        val outputReportFile = File(dotCoverResultsDir, "CoverageReport_${buildStepId}.xml")
        val dotCoverReportProjectFile = File(agentTmp,"report_$DOTCOVER_CONFIG_EXTENSION")
        every { _dotCoverSettings.buildStepId } returns buildStepId
        every { _dotCoverSettings.additionalSnapshotPaths } returns emptySequence()
        every { _pathService.getTempFileName("report_${DOTCOVER_CONFIG_EXTENSION}") } returns dotCoverReportProjectFile
        every { _virtualContext.resolvePath(dotCoverReportProjectFile.path) } returns dotCoverReportProjectFile.path
        val dotCoverMergeProject = DotCoverProject(DotCoverCommandType.Report, coverCommandData = null, mergeCommandData  = null,
            ReportCommandData(sourceFile = Path(outputSnapshotFile.path), outputFile = Path(outputReportFile.path))
        )
        every { _dotCoverProjectSerializer.serialize(dotCoverMergeProject, any()) } returns Unit
        every { _virtualContext.resolvePath(dotCoverResultsDir.path) } returns dotCoverResultsDir.path
        every { _virtualContext.resolvePath(outputSnapshotFile.absolutePath) } returns outputSnapshotFile.absolutePath
        every { _virtualContext.resolvePath(outputReportFile.absolutePath) } returns outputReportFile.absolutePath

        val expectedWorkflow = Workflow(
            sequenceOf(
                CommandLine(
                    baseCommandLine = null,
                    target = TargetType.CodeCoverageProfiler,
                    executableFile = dotCoverExecutableVirtualPath,
                    workingDirectory = Path(workingDir.absolutePath),
                    arguments = listOf(
                        CommandLineArgument("report", CommandLineArgumentType.Mandatory),
                        CommandLineArgument(dotCoverReportProjectFile.absolutePath, CommandLineArgumentType.Target)
                    ),
                    environmentVariables = _defaultVariables.toList(),
                    title = "dotCover report")
            ))
        val composer = createInstance(fileSystemService)

        // Act
        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit, Workflow()).commandLines.toList()

        // Assert
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createSnapshots(numberOfFiles: Int = 1, snapshotsDirectory: File = agentTmp): List<Path> {
        val result = ArrayList<Path>()
        for (i in 1..numberOfFiles) {
            val file = File(snapshotsDirectory, "$i.dcvr")
            file.createNewFile()
            result.add(Path(file.path))
            every { _virtualContext.resolvePath(file.absolutePath) } returns file.absolutePath
        }
        return result
    }

    private fun createInstance(fileSystemService: FileSystemService): BuildStepPostProcessingWorkflowComposer {
        return DotCoverReportingWorkflowComposer(
            _pathService,
            _parametersService,
            fileSystemService,
            _dotCoverProjectSerializer,
            _loggerService,
            _virtualContext,
            _environmentVariables,
            _entryPointSelector,
            _dotCoverSettings,
            listOf(
                DotCoverCoverCommandLineBuilder(_pathService, _virtualContext, _parametersService, fileSystemService, _argumentsService, _buildStepContext, _monoToolProvider),
                DotCoverMergeCommandLineBuilder(_pathService, _virtualContext, _parametersService, fileSystemService),
                DotCoverReportCommandLineBuilder(_pathService, _virtualContext, _parametersService, fileSystemService)
            ),
            _dotCoverTeamCityReportGenerator,
            _dotnetCoverageStatisticsPublisher,
            _uploader)
    }
}
