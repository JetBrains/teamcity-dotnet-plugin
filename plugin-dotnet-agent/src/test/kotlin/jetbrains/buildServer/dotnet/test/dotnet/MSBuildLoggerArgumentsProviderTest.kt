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
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity;params")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Normal,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity;verbosity=normal;params")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Quiet,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity;verbosity=quiet;params")),
                arrayOf(
                        File("logger.dll") as File?,
                        Verbosity.Minimal,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity;verbosity=minimal;params")))
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            verbosity: Verbosity?,
            expectedArguments: List<String>) {
        // Given
        val ctx = Mockery()
        val context = DotnetBuildContext(File("wd"), ctx.mock(DotnetCommand::class.java), DotnetSdk(File("dotnet"), Version.Empty))
        val loggerParameters = ctx.mock(LoggerParameters::class.java)
        val argumentsProvider = MSBuildLoggerArgumentsProvider(LoggerResolverStub(loggerFile, File("vstestlogger")), loggerParameters)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<LoggerParameters>(loggerParameters).msBuildLoggerVerbosity
                will(returnValue(verbosity))

                oneOf<LoggerParameters>(loggerParameters).msBuildParameters
                will(returnValue("params"))
            }
        })
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}