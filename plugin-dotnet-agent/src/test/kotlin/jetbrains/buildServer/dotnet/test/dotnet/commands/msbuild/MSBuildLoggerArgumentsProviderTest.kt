

package jetbrains.buildServer.dotnet.test.dotnet.commands.msbuild

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildLoggerArgumentsProvider
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.test.dotnet.logging.LoggerResolverStub
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
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;additionalParam;params\"")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Normal,
                        listOf(
                                "/noconsolelogger",
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=normal;additionalParam;params\"")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Quiet,
                        listOf(
                                "/noconsolelogger",
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=quiet;additionalParam;params\"")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Minimal,
                        listOf(
                                "/noconsolelogger",
                                "\"/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,v_${File("logger.dll").absolutePath};TeamCity;verbosity=minimal;additionalParam;params\"")))
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            verbosity: Verbosity?,
            expectedArguments: List<String>) {
        // Given
        val context = DotnetCommandContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val argumentsProvider = MSBuildLoggerArgumentsProvider(LoggerResolverStub(loggerFile, File("vstestlogger")),_loggerParameters, _virtualContext)
        every { _loggerParameters.msBuildLoggerVerbosity } returns verbosity
        every { _loggerParameters.msBuildParameters } returns "params"
        every { _loggerParameters.getAdditionalLoggerParameters(context) } returns sequenceOf("additionalParam")

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}