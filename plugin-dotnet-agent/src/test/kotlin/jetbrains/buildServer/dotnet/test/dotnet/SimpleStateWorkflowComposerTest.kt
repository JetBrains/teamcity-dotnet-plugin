

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

class SimpleStateWorkflowComposerTest {
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var my_pathResolverWorkflowComposer: PathResolverWorkflowComposer
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

        val composer = createInstance()
        val executable = ToolPath(Path("dotnet"), Path("v_dotnet"), homePaths)
        val state = ToolState(executable, _pathSubject, _versionSubject)

        // When
        val actualWorkflow = composer.compose(_context, state)

        // Then
        Assert.assertEquals(actualWorkflow.commandLines.toList(), emptyList<CommandLine>())
        Assert.assertEquals(_versions, listOf(Version.Empty))
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
        every { my_pathResolverWorkflowComposer.compose(_context, capture(pathResolverStates)) } answers { Workflow(commandLines) }

        val composer = createInstance()
        val executable = ToolPath(Path("dotnet"), Path("v_dotnet"), emptyList())
        val state = ToolState(executable, _pathSubject, _versionSubject)

        // When
        val actualWorkflow = composer.compose(_context, state)

        // Then
        Assert.assertEquals(actualWorkflow.commandLines.toList(), listOf(whichCommandline))
        Assert.assertEquals(_versions, listOf(Version.Empty))
        Assert.assertEquals(_paths, listOf(Path("resolved_dotnet")))
    }

    private fun createInstance() =
            SimpleStateWorkflowComposer(
                    _virtualContext,
                    listOf(my_pathResolverWorkflowComposer))
}