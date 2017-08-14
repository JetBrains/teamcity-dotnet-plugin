package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.arguments.RestoreArgumentsProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RestoreArgumentsProviderTest {
    @DataProvider
    fun testRestoreArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("restore", "path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_RESTORE_PACKAGES, "packages/"),
                        Pair(DotnetConstants.PARAM_RESTORE_PARALLEL, "false")),
                        listOf("restore", "--packages", "packages/")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_PARALLEL, "true")), listOf("restore", "--disable-parallel")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com")),
                        listOf("restore", "--source", "http://jb.com")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com\nhttp://jb.ru")),
                        listOf("restore", "--source", "http://jb.com", "--source", "http://jb.ru")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com http://jb.ru")),
                        listOf("restore", "--source", "http://jb.com", "--source", "http://jb.ru")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_RESTORE_NO_CACHE to " tRue",
                        DotnetConstants.PARAM_RESTORE_IGNORE_FAILED to "True ",
                        DotnetConstants.PARAM_RESTORE_ROOT_PROJECT to "true"),
                        listOf("restore", "--no-cache", "--ignore-failed-sources", "--no-dependencies")))
    }

    @Test(dataProvider = "testRestoreArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = RestoreArgumentsProvider(ParametersServiceStub(parameters), ArgumentsServiceStub())

        // When
        val actualArguments = argumentsProvider.getArguments().map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}