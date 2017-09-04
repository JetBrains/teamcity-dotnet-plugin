package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.BuildArgumentsProvider
import jetbrains.buildServer.dotnet.arguments.CleanArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class CleanArgumentsProviderTest {
    @DataProvider
    fun testCleanArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_CLEAN_FRAMEWORK, "dnxcore50"),
                        Pair(DotnetConstants.PARAM_CLEAN_CONFIG, "Release"),
                        Pair(DotnetConstants.PARAM_CLEAN_RUNTIME, "win7-x64")),
                        listOf("--framework", "dnxcore50", "--configuration", "Release", "--runtime", "win7-x64")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_CLEAN_OUTPUT, "output/")),
                        listOf("--output", "output/")))
    }

    @Test(dataProvider = "testCleanArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = CleanArgumentsProvider(ParametersServiceStub(parameters), ArgumentsServiceStub())

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}