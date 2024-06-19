package jetbrains.buildServer.dotnet.test.nunit.arguments

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.nunit.NUnitSettings
import jetbrains.buildServer.nunit.arguments.NUnitTestingAssembliesProvider
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class NUnitTestingAssembliesProviderTest {
    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _pathMatcher: PathMatcher

    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _pathsService: PathsService


    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should normalize include, exclude patterns by pass it to ant path matcher`() {
        // arrange
        val checkoutDir = File("checkout-dir")
        val expectedAssemblies = listOf(File("assembly1.dll"), File("assembly2.dll"))
        every { _pathMatcher.match(any(), any(), any()) } returns expectedAssemblies
        every { _pathsService.getPath(PathType.Checkout) } returns checkoutDir

        _nUnitSettings.let {
            every { it.includeTestFiles } returns "first\\line\\include\nsecond-line-include"
            every { it.excludeTestFiles } returns "first/line/exclude\nsecond-line-exclude"
        }

        val provider = NUnitTestingAssembliesProvider(
            _nUnitSettings,
            _pathMatcher,
            _loggerService,
            _pathsService
        )

        // act
        val assemblies = provider.assemblies

        // assert
        assertEquals(assemblies.size, expectedAssemblies.size)
        verify(exactly = 1) {
            _pathMatcher.match(
                checkoutDir,
                listOf(
                    listOf("first", "line", "include").joinToString(File.separator),
                    "second-line-include"
                ),
                listOf(
                    listOf("first", "line", "exclude").joinToString(File.separator),
                    "second-line-exclude"
                )
            )
        }
    }

    @Test
    fun `should report build problem if no assemblies are found`() {
        // arrange
        every { _pathMatcher.match(any(), any(), any()) } returns emptyList()
        every { _pathsService.getPath(PathType.Checkout) } returns File("checkout-dir")

        _nUnitSettings.let {
            every { it.includeTestFiles } returns ""
            every { it.excludeTestFiles } returns ""
        }
        justRun { _loggerService.writeBuildProblem(any(), any(), any()) }

        val provider = NUnitTestingAssembliesProvider(
            _nUnitSettings,
            _pathMatcher,
            _loggerService,
            _pathsService
        )

        // act
        val assemblies = provider.assemblies

        // assert
        assertEquals(assemblies.size, 0)
        verify { _loggerService.writeBuildProblem("NO_TEST_ASSEMBLIES", any(), any()) }
    }
}