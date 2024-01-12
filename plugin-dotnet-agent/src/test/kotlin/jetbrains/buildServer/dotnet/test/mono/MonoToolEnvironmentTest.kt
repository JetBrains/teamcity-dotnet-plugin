

package jetbrains.buildServer.dotnet.test.mono

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.mono.MonoToolEnvironment
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MonoToolEnvironmentTest {
    @MockK private lateinit var _buildStepContext: BuildStepContext
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "homePaths")
    fun homePaths(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(null, emptyList<Path>()),
                arrayOf("Program", listOf(Path("Program"), Path("Program${File.separatorChar}bin"))))
    }

    @Test(dataProvider = "homePaths")
    fun shouldProvideHomePathsWhenOutOfBuildStep(homeVar: String?, expectedPaths: List<File>) {
        // Given
        every { _buildStepContext.isAvailable } returns false
        every { _environment.tryGetVariable(MonoConstants.TOOL_HOME) } returns homeVar

        // When
        val actualPaths = createInstance().homePaths.toList()

        // Then
        Assert.assertEquals(actualPaths, expectedPaths)
    }

    @Test(dataProvider = "homePaths")
    fun shouldProvideHomePathsWhenInfBuildStep(homeVar: String?, expectedPaths: List<File>) {
        // Given
        every { _buildStepContext.isAvailable } returns true
        every { _parametersService.tryGetParameter(ParameterType.Environment, MonoConstants.TOOL_HOME) } returns homeVar

        // When
        val actualPaths = createInstance().homePaths.toList()

        // Then
        Assert.assertEquals(actualPaths, expectedPaths)
    }

    @Test
    fun shouldProvideDefaultPaths() {
        // Given

        // When
        val actualPaths = createInstance().defaultPaths.toList()

        // Then
        Assert.assertEquals(actualPaths, emptyList<Path>())
    }

    @Test
    fun shouldProvideEnvironmentPaths() {
        // Given
        every { _environment.paths } returns sequenceOf(Path("a"), Path("B"))

        // When
        val actualPaths = createInstance().environmentPaths.toList()

        // Then
        Assert.assertEquals(actualPaths, listOf(Path("a"), Path("B"), Path("a${File.separatorChar}bin"), Path("B${File.separatorChar}bin")))
    }

    private fun createInstance() =
            MonoToolEnvironment(_buildStepContext, _environment, _parametersService)
}