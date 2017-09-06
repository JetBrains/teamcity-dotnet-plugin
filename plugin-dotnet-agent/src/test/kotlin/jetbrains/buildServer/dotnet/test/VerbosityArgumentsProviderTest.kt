package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.VerbosityArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class VerbosityArgumentsProviderTest {
    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VERBOSITY to "normal"),
                        listOf("--verbosity", "normal")),
                arrayOf(emptyMap<String, String>(),
                        emptyList<String>()),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VERBOSITY to "detailed"),
                        listOf("--verbosity", "detailed")))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = VerbosityArgumentsProvider(ParametersServiceStub(parameters), ArgumentsServiceStub())

        // When
        val actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}