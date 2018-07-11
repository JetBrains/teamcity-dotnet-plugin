package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildLoggerArgumentsProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(
                        File("logger.dll") as File?,
                        null,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Normal,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity;verbosity=normal")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Quiet,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity;verbosity=quiet")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Minimal,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity;verbosity=minimal")))
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            verbosity: Verbosity?,
            expectedArguments: List<String>) {
        // Given
        val ctx = Mockery()
        val context = DotnetBuildContext(ctx.mock(DotnetCommand::class.java))
        val loggerParameters = ctx.mock(LoggerParameters::class.java)
        val argumentsProvider = MSBuildLoggerArgumentsProvider(LoggerResolverStub(loggerFile, File("vstestlogger")), loggerParameters)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerParameters>(loggerParameters).msBuildLoggerVerbosity
                will(returnValue(verbosity))
            }
        })
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}