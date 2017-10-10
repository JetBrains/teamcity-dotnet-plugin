package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.MSBuildVSTestLoggerParametersProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildVSTestLoggerParametersProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        listOf(MSBuildParameter("VSTestLogger", "logger://teamcity"), MSBuildParameter("VSTestTestAdapterPath", ".")))
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            expectedParameters: List<MSBuildParameter>) {
        // Given
        val argumentsProvider = MSBuildVSTestLoggerParametersProvider(LoggerResolverStub(File("msbuildlogger"), loggerFile))

        // When
        val actualParameters = argumentsProvider.parameters.toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}