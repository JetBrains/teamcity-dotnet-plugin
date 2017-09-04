package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.RunArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RunArgumentsProviderTest {
    @DataProvider
    fun testRunArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "path/"),
                        listOf("--project", "path/")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_RUN_FRAMEWORK to "dotcore",
                        DotnetConstants.PARAM_RUN_CONFIG to "Release"),
                        listOf("--framework", "dotcore", "--configuration", "Release")))
    }

    @Test(dataProvider = "testRunArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = RunArgumentsProvider(ParametersServiceStub(parameters))

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}