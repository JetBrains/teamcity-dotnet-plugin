package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetLoggerProvider
import jetbrains.buildServer.dotnet.Logger
import jetbrains.buildServer.dotnet.arguments.MSBuildLoggerArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildLoggerArgumentsProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Has no logger path
                arrayOf(null as File?, emptyList<String>()),

                // Success scenario
                arrayOf(
                        File("logger.dll") as File?,
                        listOf(
                                "/noconsolelogger",
                                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${File("logger.dll").absolutePath};TeamCity"))
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerPath: File?,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = MSBuildLoggerArgumentsProvider(object : DotnetLoggerProvider {
            override fun tryGetToolPath(logger: Logger): File? {
                Assert.assertEquals(logger, Logger.MSBuildLogger15)
                return loggerPath
            }
        })

        // When
        var actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}