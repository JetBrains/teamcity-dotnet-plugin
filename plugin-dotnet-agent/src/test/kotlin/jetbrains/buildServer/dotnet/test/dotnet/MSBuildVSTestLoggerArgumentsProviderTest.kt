package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.MSBuildVSTestLoggerArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildVSTestLoggerArgumentsProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Has no logger path
                arrayOf(null as File?, emptyList<String>()),

                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        listOf(
                                "/p:VSTestLogger=logger://teamcity",
                                "/p:VSTestTestAdapterPath=${File("loggerPath").absolutePath}"))
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File?,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = MSBuildVSTestLoggerArgumentsProvider(LoggerResolverStub(File("msbuildlogger"), loggerFile))

        // When
        var actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}