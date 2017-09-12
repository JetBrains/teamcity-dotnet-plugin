package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.VSTestLoggerArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestLoggerArgumentsProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Has no logger path
                arrayOf(null as File?, emptyList<String>()),

                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        listOf(
                                "/logger:logger://teamcity",
                                "/TestAdapterPath:${File("loggerPath").absolutePath}"))
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File?,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = VSTestLoggerArgumentsProvider(LoggerResolverStub(File("msbuildlogger"), loggerFile))

        // When
        var actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}