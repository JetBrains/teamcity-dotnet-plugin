package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.NugetPushArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NugetPushArgumentsProviderTest {
    @DataProvider
    fun testNugetPushArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "package.nupkg",
                        DotnetConstants.PARAM_NUGET_PUSH_API_KEY to "key",
                        DotnetConstants.PARAM_NUGET_PUSH_SOURCE to "http://jb.com"),
                        listOf("nuget", "push", "package.nupkg", "--api-key", "key", "--source", "http://jb.com")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "package.nupkg",
                        DotnetConstants.PARAM_NUGET_PUSH_NO_BUFFER to "true",
                        DotnetConstants.PARAM_NUGET_PUSH_NO_SYMBOLS to "true"),
                        listOf("nuget", "push", "package.nupkg", "--no-symbols", "true", "--disable-buffering", "true"))
        )
    }

    @Test(dataProvider = "testNugetPushArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = NugetPushArgumentsProvider(ParametersServiceStub(parameters))

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}