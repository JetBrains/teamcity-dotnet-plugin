package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetCommonArgumentsProviderImpl
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommonArgumentsProviderTest {
    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VERBOSITY to Verbosity.Normal.id),
                        listOf("--verbosity", Verbosity.Normal.id)),
                arrayOf(emptyMap<String, String>(),
                        emptyList<String>()),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VERBOSITY to Verbosity.Detailed.id),
                        listOf("--verbosity", Verbosity.Detailed.id)))
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