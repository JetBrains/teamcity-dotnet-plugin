package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.runners.CommandLineArgument
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NugetPushCommandTest {
    @DataProvider
    fun testNugetPushArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "package.nupkg",
                        DotnetConstants.PARAM_NUGET_PUSH_API_KEY to "key",
                        DotnetConstants.PARAM_NUGET_PUSH_SOURCE to "http://jb.com"),
                        listOf("--api-key", "key", "--source", "http://jb.com", "customArg1")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "package.nupkg",
                        DotnetConstants.PARAM_NUGET_PUSH_NO_BUFFER to "true",
                        DotnetConstants.PARAM_NUGET_PUSH_NO_SYMBOLS to "true"),
                        listOf("--no-symbols", "true", "--disable-buffering", "true", "customArg1"))
        )
    }

    @Test(dataProvider = "testNugetPushArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters=parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        val actualArguments = command.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf<String>("my.nupkg") as Any, listOf<List<String>>(listOf<String>("my.nupkg"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf<String>("my.nupkg", "my2.nupkg") as Any, listOf<List<String>>(listOf<String>("my.nupkg"), listOf<String>("my2.nupkg"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val command = createCommand(targets = targets.asSequence())

        // When
        val args = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(args, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = createCommand()

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.NuGetPush)
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
        val command = createCommand()

        // When
        val actualResult = command.isSuccessfulExitCode(exitCode)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    @Test
    fun shouldProvideToolExecutableFile() {
        // Given
        val command = createCommand()

        // When
        val actualToolExecutableFile = command.toolResolver.executableFile

        // Then
        Assert.assertEquals(actualToolExecutableFile, File("dotnet"))
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence()): DotnetCommand =
            NugetPushCommand(
                    ParametersServiceStub(parameters),
                    TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                    DotnetCommonArgumentsProviderStub(arguments),
                    DotnetToolResolverStub(File("dotnet"), true))
}