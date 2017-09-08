package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.runners.CommandLineArgument
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class BuildCommandTest {
    @DataProvider
    fun testBuildArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        listOf("customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_BUILD_FRAMEWORK, "dnxcore50"),
                        Pair(DotnetConstants.PARAM_BUILD_CONFIG, "Release")),
                        listOf("--framework", "dnxcore50", "--configuration", "Release", "customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_BUILD_OUTPUT, "output/")),
                        listOf("--output", "output/", "customArg1")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_BUILD_NON_INCREMENTAL to " true",
                        DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES to "True "),
                        listOf("--no-incremental", "--no-dependencies", "customArg1")))
    }

    @Test(dataProvider = "testBuildArgumentsData")
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
                arrayOf(listOf<String>("my.csproj") as Any, listOf<List<String>>(listOf<String>("my.csproj"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf<String>("my.csproj", "my2.csproj") as Any, listOf<List<String>>(listOf<String>("my.csproj"), listOf<String>("my2.csproj"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val command = createCommand(targets = targets.asSequence())

        // When
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = createCommand()

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.Build)
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
            BuildCommand(
                    ParametersServiceStub(parameters),
                    TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                    DotnetCommonArgumentsProviderStub(arguments),
                    DotnetToolResolverStub(File("dotnet"), true))
}