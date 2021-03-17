package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

class InspectionWorkflowComposerTest {
    @MockK private lateinit var _toolPathResolver: ProcessResolver
    @MockK private lateinit var _argumentsProvider: ArgumentsProvider
    @MockK private lateinit var _environmentProvider: EnvironmentProvider
    @MockK private lateinit var _outputObserver: OutputObserver
    @MockK private lateinit var _configurationFile: ConfigurationFile
    @MockK private lateinit var _buildInfo: BuildInfo
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _artifacts: ArtifactService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _context: WorkflowContext

    private val _process = InspectionProcess(Path("inspection"), listOf(CommandLineArgument("exec"),  CommandLineArgument("--")))
    private val _workingDirectory = Path("wd")
    private val _events = mutableListOf<CommandResultEvent>()
    private val _envVar = CommandLineEnvironmentVariable("var1", "val1")
    private val _outputStream = ByteArrayOutputStream()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _toolPathResolver.resolve(InspectionTool.Inspectcode) } returns _process
        every { _pathsService.getPath(PathType.Checkout) } returns File(_workingDirectory.path)
        every { _buildInfo.runType } returns InspectionTool.Inspectcode.runnerType
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
        every { _fileSystemService.write(any(), any()) } answers { arg<(OutputStream) -> Unit>(1) (_outputStream) }
        every { _configurationFile.create(any(), any(), any(), any()) } returns Unit
        every { _outputObserver.onNext(any()) } returns Unit
        every { _artifacts.publish(any(), any(), any()) } returns true
        every { _loggerService.importData(any(), any(), any()) } returns Unit
        every { _loggerService.buildFailureDescription(any()) } returns Unit
        every { _environmentProvider.getEnvironmentVariables() } returns sequenceOf(_envVar)

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
                                listOf(CommandLineArgument("--arg1"))),
                        0,
                        listOf(
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
                                                CommandLineArgument("--arg1")
                                        ),
                                        listOf(_envVar)))
                ),
                arrayOf(
                        InspectionArguments(
                                File("Config.xml"),
                                File("Output.xml"),
                                File("Log.txt"),
                                null,
                                false,
                                listOf(CommandLineArgument("--arg1"))),
                        0,
                        listOf(
                                CommandLine(
                                        null,
                                        TargetType.Tool,
                                        _process.executable,
                                        _workingDirectory,
                                        listOf(
                                                CommandLineArgument("exec"),
                                                CommandLineArgument("--"),
                                                CommandLineArgument("--config=v_${File("Config.xml").absolutePath}"),
                                                CommandLineArgument("--arg1")
                                        ),
                                        listOf(_envVar)))
                ),
                arrayOf(
                        InspectionArguments(
                                File("Config.xml"),
                                File("Output.xml"),
                                File("Log.txt"),
                                null,
                                false,
                                emptyList()),
                        0,
                        listOf(
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
                                        listOf(_envVar)))
                ),
                arrayOf(
                        InspectionArguments(
                                File("Config.xml"),
                                File("Output.xml"),
                                File("Log.txt"),
                                File("Cache"),
                                true,
                                listOf(CommandLineArgument("--arg1"))),
                        1,
                        listOf(
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
                                                CommandLineArgument("--arg1")
                                        ),
                                        listOf(_envVar)))
                )
        )
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(args: InspectionArguments, exitCode: Int, expectedCommandLines: List<CommandLine>) {
        // Given
        val composer = createInstance()
        every { _argumentsProvider.getArguments(InspectionTool.Inspectcode) } returns args
        _events.add(CommandResultOutput("Line 1"))
        _events.add(CommandResultExitCode(exitCode))

        // When
        var actualCommandLines = composer.compose(_context, Unit).commandLines.toList()

        // Then
        Assert.assertEquals(actualCommandLines, expectedCommandLines)
        verify { _fileSystemService.write(args.configFile, any()) }
        verify { _configurationFile.create(_outputStream, Path("v_" + args.outputFile.absolutePath), args.cachesHome?.let{ Path("v_" + it.absolutePath)}, args.debug) }
        verify { _outputObserver.onNext("Line 1") }
        if(args.debug) {
            verify { _artifacts.publish(InspectionTool.Inspectcode, args.logFile, null) }
        }

        if(exitCode == 0) {
            verify { _loggerService.importData(InspectionTool.Inspectcode.dataProcessorType, Path("v_" + args.outputFile.absolutePath)) }
        } else {
            verify { _loggerService.buildFailureDescription("${InspectionTool.Inspectcode.dysplayName} execution failure.") }
            verify { _context.abort(BuildFinishedStatus.FINISHED_FAILED) }
        }
    }

    @Test
    fun shouldFailBuildWhenCanntFindOutputFile() {
        // Given
        val args = InspectionArguments(
                File("Config.xml"),
                File("Output.xml"),
                File("Log.txt"),
                null,
                false,
                emptyList()
        )

        val composer = createInstance()
        every { _argumentsProvider.getArguments(InspectionTool.Inspectcode) } returns args
        _events.add(CommandResultOutput("Line 1"))
        _events.add(CommandResultExitCode(0))

        // When
        every { _artifacts.publish(any(), any(), any()) } returns false
        composer.compose(_context, Unit).commandLines.toList()

        // Then
        verify { _loggerService.buildFailureDescription("Output xml from ${InspectionTool.Inspectcode.dysplayName} is not found or empty on path ${args.outputFile.canonicalPath}.") }
        verify { _context.abort(BuildFinishedStatus.FINISHED_FAILED) }
    }

    private fun createInstance() =
            InspectionWorkflowComposer(
                    InspectionTool.Inspectcode,
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
                    _virtualContext)
}