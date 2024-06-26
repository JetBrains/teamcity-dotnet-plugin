package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.ToolState
import jetbrains.buildServer.dotnet.test.rx.assertEquals
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.serverSide.impl.agent.PollingRemoteAgentConnection.Command
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

class InspectionWorkflowComposerTest {
    @MockK private lateinit var _toolPathResolver: ToolStartInfoResolver
    @MockK private lateinit var _argumentsProvider: ArgumentsProvider
    @MockK private lateinit var _environmentProvider: EnvironmentProvider
    @MockK private lateinit var _outputObserver: OutputObserver
    @MockK private lateinit var _configurationFile: ConfigurationFile
    @MockK private lateinit var _buildInfo: BuildInfo
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _artifacts: ArtifactService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _context: WorkflowContext
    @MockK private lateinit var _stateWorkflowComposer: InspectionToolStateWorkflowComposer

    private val _process = ToolStartInfo(Path("inspection"), InspectionToolPlatform.CrossPlatform, listOf(CommandLineArgument("exec"), CommandLineArgument("--")))
    private val _workingDirectory = Path("wd")
    private val _events = mutableListOf<CommandResultEvent>()
    private val _envVar = CommandLineEnvironmentVariable("var1", "val1")
    private val _outputStream = ByteArrayOutputStream()
    private val _toolStateCommandLine = CommandLine(null, TargetType.Tool, _process.executable, _workingDirectory, listOf(CommandLineArgument("--version")), emptyList())

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _toolPathResolver.resolve(InspectionTool.Inspectcode) } returns _process
        every { _pathsService.getPath(PathType.Checkout) } returns File(_workingDirectory.path)
        every { _buildInfo.runType } returns InspectionTool.Inspectcode.runnerType
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
        every { _fileSystemService.write<Unit>(any(), any()) } answers { arg<(OutputStream) -> Unit>(1)(_outputStream) }
        every { _configurationFile.create(any(), any(), any(), any()) } returns Unit
        every { _outputObserver.onNext(any()) } returns Unit
        every { _artifacts.publish(any(), any(), any()) } returns true
        every { _loggerService.importData(any(), any(), any()) } returns Unit
        every { _loggerService.buildFailureDescription(any()) } returns Unit
        every { _loggerService.writeWarning(any()) } returns Unit
        every { _environmentProvider.getEnvironmentVariables(any(), any()) } returns sequenceOf(_envVar)
        every { _stateWorkflowComposer.compose(any(), any()) } returns Workflow(sequenceOf(_toolStateCommandLine))

        every { _context.subscribe(any()) } answers {
            val observer = arg<Observer<CommandResultEvent>>(0)
            for (e in _events) {
                observer.onNext(e)
            }

            emptyDisposable()
        }

        every { _context.abort(any()) } returns Unit

        _events.clear()
    }

    @DataProvider
    fun composeCases(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf(
                InspectionArguments(
                    File("Config.xml"),
                    File("Output.xml"),
                    File("Log.txt"),
                    File("Cache"),
                    true,
                    "Dedicated.Extension/1.0.0",
                    listOf(CommandLineArgument("--arg1")),
                ),
                0,
                listOf(
                    _toolStateCommandLine,
                    CommandLine(
                        null,
                        TargetType.Tool,
                        _process.executable,
                        _workingDirectory,
                        listOf(
                            CommandLineArgument("exec"),
                            CommandLineArgument("--"),
                            CommandLineArgument("--config=v_${File("Config.xml").absolutePath}"),
                            CommandLineArgument("--logFile=v_${File("Log.txt").absolutePath}"),
                            CommandLineArgument("--eXtensions=Dedicated.Extension/1.0.0"),
                            CommandLineArgument("--arg1")
                        ),
                        listOf(_envVar)
                    )
                )
            ),
            arrayOf(
                InspectionArguments(
                    File("Config.xml"),
                    File("Output.xml"),
                    File("Log.txt"),
                    File("Cache"),
                    false,
                    "Dedicated.Extension/1.0.0",
                    listOf(CommandLineArgument("--arg1"))
                ),
                0,
                listOf(
                    _toolStateCommandLine,
                    CommandLine(
                        null,
                        TargetType.Tool,
                        _process.executable,
                        _workingDirectory,
                        listOf(
                            CommandLineArgument("exec"),
                            CommandLineArgument("--"),
                            CommandLineArgument("--config=v_${File("Config.xml").absolutePath}"),
                            CommandLineArgument("--eXtensions=Dedicated.Extension/1.0.0"),
                            CommandLineArgument("--arg1")
                        ),
                        listOf(_envVar)
                    )
                )
            ),
            arrayOf(
                InspectionArguments(
                    File("Config.xml"),
                    File("Output.xml"),
                    File("Log.txt"),
                    File("Cache"),
                    false,
                    null,
                    emptyList()
                ),
                0,
                listOf(
                    _toolStateCommandLine,
                    CommandLine(
                        null,
                        TargetType.Tool,
                        _process.executable,
                        _workingDirectory,
                        listOf(
                            CommandLineArgument("exec"),
                            CommandLineArgument("--"),
                            CommandLineArgument("--config=v_${File("Config.xml").absolutePath}")
                        ),
                        listOf(_envVar)
                    )
                )
            ),
            arrayOf(
                InspectionArguments(
                    File("Config.xml"),
                    File("Output.xml"),
                    File("Log.txt"),
                    File("Cache"),
                    true,
                    null,
                    listOf(CommandLineArgument("--arg1"), CommandLineArgument("--eXtensions=Custom.Extension/2.0.0"))
                ),
                1,
                listOf(
                    _toolStateCommandLine,
                    CommandLine(
                        null,
                        TargetType.Tool,
                        _process.executable,
                        _workingDirectory,
                        listOf(
                            CommandLineArgument("exec"),
                            CommandLineArgument("--"),
                            CommandLineArgument("--config=v_${File("Config.xml").absolutePath}"),
                            CommandLineArgument("--logFile=v_${File("Log.txt").absolutePath}"),
                            CommandLineArgument("--arg1"),
                            CommandLineArgument("--eXtensions=Custom.Extension/2.0.0")
                        ),
                        listOf(_envVar)
                    )
                )
            )
        )
    }

    @Test(dataProvider = "composeCases")
    fun `should compose`(args: InspectionArguments, exitCode: Int, expectedCommandLines: List<CommandLine>) {
        // arrange
        val composer = createInstance(InspectionTool.Inspectcode) {
            _events.add(CommandResultOutput("Line 1", mutableListOf(), it.Id))
            _events.add(CommandResultExitCode(exitCode, it.Id))
            it
        }
        every { _argumentsProvider.getArguments(InspectionTool.Inspectcode, any()) } returns args

        // act
        val actualCommandLines = composer.compose(_context, Unit).commandLines.toList()

        // assert
        Assert.assertEquals(actualCommandLines, expectedCommandLines)
        verify { _fileSystemService.write(args.configFile, any()) }
        verify { _configurationFile.create(_outputStream, Path("v_" + args.outputFile.absolutePath), Path("v_" + args.cachesHome.absolutePath), args.debug) }
        verify { _outputObserver.onNext("Line 1") }
        verify(exactly = 1) { _stateWorkflowComposer.compose(any(), any()) }
        if (args.debug) {
            verify { _artifacts.publish(InspectionTool.Inspectcode, args.logFile, null) }
        }

        if (exitCode == 0) {
            verify { _loggerService.importData(InspectionTool.Inspectcode.dataProcessorType, Path("v_" + args.outputFile.absolutePath)) }
        } else {
            verify { _loggerService.buildFailureDescription("${InspectionTool.Inspectcode.displayName} execution failure.") }
            verify { _context.abort(BuildFinishedStatus.FINISHED_FAILED) }
        }
    }

    @Test
    fun `should fail build when cannot find output file`() {
        // arrange
        val args = InspectionArguments(
            File("Config.xml"),
            File("Output.xml"),
            File("Log.txt"),
            File("Cache"),
            false,
            null,
            emptyList()
        )

        val composer = createInstance(InspectionTool.Inspectcode) {
            _events.add(CommandResultOutput("Line 1", mutableListOf(), it.Id))
            _events.add(CommandResultExitCode(0, it.Id))
            it
        }
        every { _argumentsProvider.getArguments(InspectionTool.Inspectcode, any()) } returns args
        every { _artifacts.publish(any(), any(), any()) } returns false

        // act
        composer.compose(_context, Unit).commandLines.toList()

        // assert
        verify { _loggerService.buildFailureDescription("Output xml from ${InspectionTool.Inspectcode.displayName} is not found or empty on path ${args.outputFile.canonicalPath}.") }
        verify { _context.abort(BuildFinishedStatus.FINISHED_FAILED) }
    }

    @Test
    fun `should fail build when has some std errors`() {
        // arrange
        val composer = createInstance(InspectionTool.Inspectcode) {
            _events.add(CommandResultOutput("Line 1", mutableListOf(), it.Id))
            _events.add(CommandResultError("Some error", mutableListOf(), it.Id))
            _events.add(CommandResultExitCode(0, it.Id))
            it
        }
        val args = InspectionArguments(
            File("Config.xml"),
            File("Output.xml"),
            File("Log.txt"),
            File("Cache"),
            true,
            null,
            listOf(CommandLineArgument("--arg1"))
        )
        every { _argumentsProvider.getArguments(InspectionTool.Inspectcode, any()) } returns args

        // act
        composer.compose(_context, Unit).commandLines.toList()

        // assert
        verify { _context.abort(BuildFinishedStatus.FINISHED_FAILED) }
    }

    // https://youtrack.jetbrains.com/issue/TW-71049
    // https://youtrack.jetbrains.com/issue/TW-71048
    @Test
    fun `should fail build with warning about NanoServer when negative exit code and In windows docker container`() {
        // arrange
        val composer = createInstance(InspectionTool.Inspectcode) {
            _events.add(CommandResultExitCode(-532462766, it.Id))
            it
        }
        val args = InspectionArguments(
            File("Config.xml"),
            File("Output.xml"),
            File("Log.txt"),
            File("Cache"),
            true,
            null,
            listOf(CommandLineArgument("--arg1"))
        )
        every { _argumentsProvider.getArguments(InspectionTool.Inspectcode, any()) } returns args

        // act
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        composer.compose(_context, Unit).commandLines.toList()

        // assert
        verify { _loggerService.writeWarning("Windows Nano Server is not supported.") }
        verify { _context.abort(BuildFinishedStatus.FINISHED_FAILED) }
    }

    @DataProvider
    fun versionComposeCases() = arrayOf(
        arrayOf(InspectionTool.Dupfinder, false),
        arrayOf(InspectionTool.Inspectcode, true)
    )

    @Test(dataProvider = "versionComposeCases")
    fun `should not request version for tool other than inspectcode`(
        inspectionTool: InspectionTool,
        hasVersionCheckCommandLine: Boolean
    ) {
        // arrange
        val composer = createInstance(inspectionTool) {
            _events.add(CommandResultOutput("Line 1", mutableListOf(), it.Id))
            _events.add(CommandResultExitCode(0, it.Id))
            it
        }
        every { _buildInfo.runType } returns inspectionTool.runnerType
        every { _toolPathResolver.resolve(inspectionTool) } returns _process
        every { _argumentsProvider.getArguments(inspectionTool, any()) } returns mockk(relaxed = true)

        // act
        val actualCommandLines = composer.compose(_context, Unit).commandLines.toList()

        // assert
        Assert.assertEquals(actualCommandLines.size, if (hasVersionCheckCommandLine) 2 else 1 )
        verify(exactly = if (hasVersionCheckCommandLine) 1 else 0) { _stateWorkflowComposer.compose(any(), any()) }
    }

    @DataProvider
    fun `tool version and whether to specify the format`() = arrayOf(
        arrayOf(Version(2024, 1, 1), true),
        arrayOf(Version(2024, 1, 0), true),
        arrayOf(Version(2024, 0), false),
        arrayOf(Version(2023, 1, 1), false),
        arrayOf(Version(2023, 1, 0), false),
    )
    @Test(dataProvider = "tool version and whether to specify the format")
    fun `should specify XML as an output format explictly when the InspetCode tool version is higher or equal than 2024_1_0`(
        toolVersion: Version,
        shouldSpecifyTheOutputFormat: Boolean
    ) {
        // arrange
        val composer = createInstance(InspectionTool.Inspectcode) {
            _events.add(CommandResultOutput("Line 1", mutableListOf(), it.Id))
            _events.add(CommandResultExitCode(0, it.Id))
            it
        }
        every { _buildInfo.runType } returns InspectionTool.Inspectcode.runnerType
        every { _toolPathResolver.resolve(any()) } returns _process
        every { _argumentsProvider.getArguments(any(), any()) } returns mockk(relaxed = true)
        every { _stateWorkflowComposer.compose(any(), any()) } answers {
            arg<InspectionToolState>(1).versionObserver.onNext(toolVersion)
            Workflow(mockk<CommandLine>())
        }

        // act
        val commandLines = composer.compose(_context, Unit).commandLines.toList()

        // assert
        verify(exactly = 1) { _stateWorkflowComposer.compose(any(), any()) }
        Assert.assertEquals(shouldSpecifyTheOutputFormat, commandLines[1].arguments.contains(CommandLineArgument("--format=Xml")))
    }

    private fun createInstance(inspectionTool: InspectionTool, onNewCommandLine: (CommandLine) -> CommandLine) =
        object : InspectionWorkflowComposer(
            inspectionTool,
            _toolPathResolver,
            _argumentsProvider,
            _environmentProvider,
            _outputObserver,
            _configurationFile,
            _buildInfo,
            _fileSystemService,
            _pathsService,
            _loggerService,
            _artifacts,
            _virtualContext,
            _stateWorkflowComposer,
        ) {
            override fun createCommandLine(toolStartInfo: ToolStartInfo, args: InspectionArguments, virtualOutputPath: Path, toolVersion: Version): CommandLine {
                return onNewCommandLine(super.createCommandLine(toolStartInfo, args, virtualOutputPath, toolVersion))
            }
        }
}