package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CustomCommandTest {
    @DataProvider
    fun testCustomArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_ARGUMENTS, "custom command arguments")),
                        listOf("custom", "command", "arguments")))
    }

    @Test(dataProvider = "testCustomArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val arguments = parameters[DotnetConstants.PARAM_ARGUMENTS]!!.split(' ').map { CommandLineArgument(it)}.asSequence()
        val command = createCommand(parameters=parameters, targets = sequenceOf("my.csproj"), arguments = arguments )

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
        Assert.assertEquals(actualCommand, DotnetCommandType.Custom)
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
            testsResultsAnalyzer: ResultsAnalyzer = TestsResultsAnalyzerStub()): DotnetCommand {
        var ctx = Mockery()
        return CustomCommand(
                ParametersServiceStub(parameters),
                testsResultsAnalyzer,
                TargetServiceStub(targets.map { CommandTarget(File(it)) }.asSequence()),
                DotnetCommonArgumentsProviderStub(arguments),
                DotnetToolResolverStub(File("dotnet"), true),
                ctx.mock<EnvironmentBuilder>(EnvironmentBuilder::class.java))
    }
}