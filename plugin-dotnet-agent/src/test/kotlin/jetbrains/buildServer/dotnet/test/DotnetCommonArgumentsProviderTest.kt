package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommonArgumentsProviderImpl
import jetbrains.buildServer.dotnet.DotnetConstants
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommonArgumentsProviderTest {
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
        val argumentsProvider = DotnetCommonArgumentsProviderImpl(ParametersServiceStub(parameters), DotnetCommonArgumentsProviderStub(), DotnetCommonArgumentsProviderStub())

        // When
        val actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}