package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.EnvironmentVariablesImpl
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class BasePathResolverWorkflowFactoryTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _workflowContext: WorkflowContext
    private val _pathEvents = mutableListOf<Notification<Path>>()
    private val _commandSubject = subjectOf<CommandResultEvent>()
    private val _workingDirectory = "wd"

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        _pathEvents.clear()

        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File(_workingDirectory)
        every { _workflowContext.subscribe(any()) } answers { _commandSubject.subscribe(arg<Observer<CommandResultEvent>>(0)) }
    }

    @Test
    fun shouldProvideCommandToResolvePath() {
        // Given
        val factory = createInstance()
        val state = PathResolverState(Path("dotnet"), _pathEvents.toObserver().dematerialize(), Path("where"))

        // When
        var actualCommandLines = factory.create(_workflowContext, state).commandLines.toList()

        // Then
        Assert.assertEquals(
                actualCommandLines,
                listOf(CommandLine(
                        null,
                        TargetType.SystemDiagnostics,
                        Path("where"),
                        Path(_workingDirectory),
                        listOf(CommandLineArgument("dotnet", CommandLineArgumentType.Target)),
                        emptyList(),
                        "get dotnet")))
    }

    @DataProvider(name = "paths")
    fun paths(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        sequenceOf<CommandResultEvent>(
                                CommandResultOutput("Access is denied"),
                                CommandResultOutput("abc/dotnet"),
                                CommandResultOutput("abc/dotnet"),
                                CommandResultOutput(""),
                                CommandResultOutput("sxs"),
                                CommandResultOutput("xyz/dotnet"),
                                CommandResultOutput("dotnet")),
                        listOf(
                                NotificationNext<Path>(Path("abc/dotnet")),
                                NotificationNext<Path>(Path("xyz/dotnet")),
                                NotificationNext<Path>(Path("dotnet")),
                                NotificationCompleted.completed<Path>())))
    }

    @Test(dataProvider = "paths")
    fun shouldResolvePath(output: Sequence<CommandResultEvent>, expectedPaths: List<NotificationNext<Path>>) {
        // Given
        val factory = createInstance()
        val state = PathResolverState(Path("dotnet"), _pathEvents.toObserver().dematerialize(), Path("where"))

        // When
        val iterator = factory.create(_workflowContext, state).commandLines.iterator()
        iterator.hasNext()
        output.toObservable().subscribe(_commandSubject);
        iterator.hasNext()

        // Then
        Assert.assertEquals(_pathEvents, expectedPaths)
    }

    private fun createInstance() = BasePathResolverWorkflowFactory(_pathsService, _virtualContext)
}