package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.TestArgumentsProvider
import jetbrains.buildServer.runners.CommandLineArgument
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestArgumentsProviderTest {
    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("test", "path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_TEST_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_TEST_CONFIG, "Release")),
                        listOf("test", "--framework", "dotcore", "--configuration", "Release")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_TEST_RUNTIME, "active"),
                        Pair(DotnetConstants.PARAM_TEST_NO_BUILD, "true")),
                        listOf("test", "--runtime", "active", "--no-build")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_OUTPUT, "out")),
                        listOf("test", "--output", "out")))
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = TestArgumentsProvider(ParametersServiceStub(parameters), object: ArgumentsProvider { override fun getArguments(): Sequence<CommandLineArgument> = emptySequence() })

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}