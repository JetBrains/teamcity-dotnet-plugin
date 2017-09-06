package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.CustomArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class CustomArgumentsProviderTest {
    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_ARGUMENTS to "arg1 arg2"),
                        listOf("arg1", "arg2")))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = CustomArgumentsProvider(ParametersServiceStub(parameters), ArgumentsServiceStub())

        // When
        val actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}