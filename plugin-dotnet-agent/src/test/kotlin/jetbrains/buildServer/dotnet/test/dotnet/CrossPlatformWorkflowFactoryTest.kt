package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CrossPlatformWorkflowFactoryTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _pathResolverWorkflowFactory: PathResolverWorkflowFactory
    @MockK private lateinit var _versionParser: VersionParser
    @MockK private lateinit var _defaultEnvironmentVariables: EnvironmentVariables
    @MockK private lateinit var _context: WorkflowContext

    private val _pathSubject = subjectOf<Path>()
    private val _paths = mutableListOf<Path>()
    private val _versionSubject = subjectOf<Version>()
    private val _versions = mutableListOf<Version>()
    private val _envVar = CommandLineEnvironmentVariable("var1", "val1")
    private val _envVars = listOf(_envVar)
    private var _token :Disposable = emptyDisposable()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        _versions.clear()
        _paths.clear()
        _token = disposableOf (
            _versionSubject.subscribe({ _versions.add(it) }),
            _pathSubject.subscribe({ _paths.add(it) })
        )

        every { _versionParser.parse(listOf("3.0.0")) } returns Version(3)
        every { _versionParser.parse(listOf(" ")) } returns Version.Empty
        every { _context.subscribe(any()) } answers {
            val observer = arg<Observer<CommandResultEvent>>(0)
            observer.onNext(CommandResultOutput("3.0.0"))
            observer.onNext(CommandResultOutput(" "))
            emptyDisposable()
        }
    }

    @AfterMethod
    fun teardown() {
        _token.dispose()
    }

    @DataProvider
    fun dotnetVersion(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(false, emptyList<Path>()),
                arrayOf(false, listOf(Path("home"))),
                arrayOf(true, listOf(Path("home"))))
    }

    @Test(dataProvider = "dotnetVersion")
    fun shouldGetDotnetVersion(isVirtual: Boolean, homePaths: List<Path>) {
        // Given
        every { _virtualContext.isVirtual } returns isVirtual
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File("wd")
        every { _defaultEnvironmentVariables.getVariables(Version.Empty) } returns _envVars.asSequence()

        val factory = createInstance()
        val executable = ToolPath(Path("dotnet"), Path("v_dotnet"), homePaths)
        val state = CrossPlatformWorkflowState(executable, _pathSubject, _versionSubject)

        // When
        val actualWorkflow = factory.create(_context, state)

        // Then
        Assert.assertEquals(
                actualWorkflow.commandLines.toList(),
                listOf(
                        CommandLine(
                                null,
                                TargetType.SystemDiagnostics,
                                executable.virtualPath,
                                Path(File("wd").canonicalPath),
                                DotnetWorkflowComposer.VersionArgs,
                                _envVars,
                                "dotnet --version",
                                listOf(StdOutText("Getting the .NET SDK version", Color.Header)))
                ))

        Assert.assertEquals(_versions, listOf(Version(3)))
    }

    @Test
    fun shouldResolveVirtualPathWhenInVirtualContextAndHomePathsAreEmpty() {
        // Given
        val whichCommandline = CommandLine(
                null,
                TargetType.SystemDiagnostics,
                Path("which"),
                Path(File("wd").canonicalPath),
                emptyList(),
                _envVars,
                "which",
                listOf(StdOutText("get dotnet")))

        every { _virtualContext.isVirtual } returns true
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File("wd")
        every { _defaultEnvironmentVariables.getVariables(Version.Empty) } returns _envVars.asSequence()
        val commandLines = mockk<Sequence<CommandLine>>()
        val commandLineIterator = mockk<Iterator<CommandLine>>()
        val pathResolverStates = mutableListOf<PathResolverState>()
        var cmdCount = 1
        every { commandLineIterator.hasNext() } answers { cmdCount -- > 0 }
        every { commandLineIterator.next() } answers {
            for (pathResolverState in pathResolverStates) {
                pathResolverState.virtualPathObserver.onNext(Path("  "))
                pathResolverState.virtualPathObserver.onNext(Path(""))
                pathResolverState.virtualPathObserver.onNext(Path("resolved_dotnet"))
                pathResolverState.virtualPathObserver.onNext(Path("abc"))
                pathResolverState.virtualPathObserver.onNext(Path("resolved_dotnet"))
                pathResolverState.virtualPathObserver.onNext(Path(""))
                pathResolverState.virtualPathObserver.onNext(Path("xyz"))
            }

            whichCommandline
        }
        every { commandLines.iterator() } returns commandLineIterator
        every { _pathResolverWorkflowFactory.create(_context, capture(pathResolverStates)) } answers { Workflow(commandLines) }

        val factory = createInstance()
        val executable = ToolPath(Path("dotnet"), Path("v_dotnet"), emptyList())
        val state = CrossPlatformWorkflowState(executable, _pathSubject, _versionSubject)

        // When
        val actualWorkflow = factory.create(_context, state)

        // Then
        Assert.assertEquals(
                actualWorkflow.commandLines.toList(),
                listOf(
                        whichCommandline,
                        CommandLine(
                                null,
                                TargetType.SystemDiagnostics,
                                Path("resolved_dotnet"),
                                Path(File("wd").canonicalPath),
                                DotnetWorkflowComposer.VersionArgs,
                                _envVars,
                                "dotnet --version",
                                listOf(StdOutText("Getting the .NET SDK version", Color.Header)))
                ))

        Assert.assertEquals(_versions, listOf(Version(3)))
        Assert.assertEquals(_paths, listOf(Path("resolved_dotnet")))
    }

    private fun createInstance() =
            CrossPlatformWorkflowFactory(
                    _pathsService,
                    _virtualContext,
                    listOf(_pathResolverWorkflowFactory),
                    _versionParser,
                    _defaultEnvironmentVariables)
}