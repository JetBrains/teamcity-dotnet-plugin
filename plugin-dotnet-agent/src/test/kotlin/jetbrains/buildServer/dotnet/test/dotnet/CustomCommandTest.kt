package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
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
        val arguments = parameters[DotnetConstants.PARAM_ARGUMENTS]!!.split(' ').map { CommandLineArgument(it) }.asSequence()
        val command = createCommand(arguments = arguments)

        // When
        val actualArguments = command.getArguments(DotnetBuildContext(File("wd"), command, DotnetSdk(File("dotnet"), Version.Empty))).map { it.value }.toList()

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
            arguments: Sequence<CommandLineArgument> = emptySequence(),
            resultsAnalyzer: ResultsAnalyzer = TestsResultsAnalyzerStub()): DotnetCommand {
        return CustomCommand(
                ParametersServiceStub(parameters),
                resultsAnalyzer,
                ArgumentsProviderStub(arguments),
                DotnetToolResolverStub(ToolPlatform.DotnetCore, File("dotnet"), true))
    }
}