package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetVSTestLoggerArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetVSTestLoggerArgumentsProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Has no logger path
                arrayOf(null as File?, emptyList<String>()),

                // Success scenario
                arrayOf(
                        File("loggerPath", "logger.dll") as File?,
                        listOf(
                                "-l=TeamCity",
                                "-a=${File("loggerPath").absolutePath}"))
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File?,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = DotnetVSTestLoggerArgumentsProvider(DotnetLoggerStub(loggerFile))

        // When
        var actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}