package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.BuildCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.NugetDeleteCommand
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NugetDeleteCommandTest {
    @DataProvider
    fun testNugetDeleteArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_NUGET_DELETE_ID to "id version",
                        DotnetConstants.PARAM_NUGET_DELETE_API_KEY to "key",
                        DotnetConstants.PARAM_NUGET_DELETE_SOURCE to "http://jb.com"),
                        listOf("id", "version", "--api-key", "key",
                                "--source", "http://jb.com", "--non-interactive"))
        )
    }

    @Test(dataProvider = "testNugetDeleteArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = NugetDeleteCommand(ParametersServiceStub(parameters))

        // When
        val actualArguments = command.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = NugetDeleteCommand(ParametersServiceStub(emptyMap()))

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.NuGetDelete)
    }

    @DataProvider
    fun checkSuccessData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, true),
                arrayOf(1, false),
                arrayOf(99, false),
                arrayOf(-1, false),
                arrayOf(-99, false))
    }

    @Test(dataProvider = "checkSuccessData")
    fun shouldImplementCheckSuccess(exitCode: Int, expectedResult: Boolean) {
        // Given
        val command = NugetDeleteCommand(ParametersServiceStub(emptyMap()))

        // When
        val actualResult = command.isSuccess(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}
