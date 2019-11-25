package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestCommandTest {
    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        listOf("customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_CONFIG, "Release")),
                        listOf("--framework", "dotcore", "--configuration", "Release", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                        listOf("--no-build", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_OUTPUT_DIR, "out")),
                        listOf("--output", "out", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")),
                        listOf("--filter", "filter", "customArg1")))
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        val actualArguments = command.getArguments(DotnetBuildContext(ToolPath(Path("wd")), command)).map { it.value }.toList()

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
        Assert.assertEquals(actualCommand, DotnetCommandType.Test)
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence(),
            testsResultsAnalyzer: ResultsAnalyzer = TestsResultsAnalyzerStub()): DotnetCommand {
        val ctx = Mockery()
        return TestCommand(
                ParametersServiceStub(parameters),
                testsResultsAnalyzer,
                TargetServiceStub(targets.map { CommandTarget(Path(it)) }.asSequence()),
                ArgumentsProviderStub(arguments),
                DotnetToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")), true),
                ctx.mock<EnvironmentBuilder>(EnvironmentBuilder::class.java))
    }

}