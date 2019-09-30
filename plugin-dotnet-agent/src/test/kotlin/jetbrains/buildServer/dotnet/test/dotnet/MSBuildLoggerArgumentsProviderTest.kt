package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildLoggerArgumentsProviderTest {
    @MockK private lateinit var _loggerParameters: LoggerParameters
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
    }

    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(
                        File("logger.dll") as File?,
                        null,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;params")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Normal,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=normal;params")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Quiet,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=quiet;params")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Minimal,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=minimal;params")))
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            verbosity: Verbosity?,
            expectedArguments: List<String>) {
        // Given
        val context = DotnetBuildContext(Path(File("wd")), mockk<DotnetCommand>())
        val argumentsProvider = MSBuildLoggerArgumentsProvider(LoggerResolverStub(loggerFile, File("vstestlogger")),_loggerParameters, _virtualContext)
        every { _loggerParameters.msBuildLoggerVerbosity } returns verbosity
        every { _loggerParameters.msBuildParameters } returns "params"

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}