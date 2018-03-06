package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Mockery
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
                        Pair(DotnetConstants.PARAM_FRAMEWORK, "dnxcore50"),
                        Pair(DotnetConstants.PARAM_CONFIG, "Release")),
                        listOf("--framework", "dnxcore50", "--configuration", "Release", "customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_OUTPUT_DIR, "output/")),
                        listOf("--output", "output/", "customArg1")))
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
                arrayOf(listOf("my.csproj") as Any, listOf(listOf("my.csproj"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf("my.csproj", "my2.csproj") as Any, listOf(listOf("my.csproj"), listOf("my2.csproj"))))
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
        val actualResult = command.isSuccessful(CommandLineResult(sequenceOf(exitCode), emptySequence(), emptySequence()))

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
            arguments: Sequence<CommandLineArgument> = emptySequence(),
            failedTestDetector: FailedTestDetector = FailedTestDetectorStub(false)): DotnetCommand {
        var ctx = Mockery()
        return BuildCommand(
                ParametersServiceStub(parameters),
                failedTestDetector,
                TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                DotnetCommonArgumentsProviderStub(arguments),
                DotnetToolResolverStub(File("dotnet"), true),
                ctx.mock<EnvironmentBuilder>(EnvironmentBuilder::class.java))
    }
}