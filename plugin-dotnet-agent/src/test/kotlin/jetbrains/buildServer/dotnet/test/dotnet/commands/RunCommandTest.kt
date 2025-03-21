

package jetbrains.buildServer.dotnet.test.dotnet.commands

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.RunCommand
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.dotnet.test.dotnet.ArgumentsProviderStub
import jetbrains.buildServer.dotnet.test.dotnet.commands.targeting.TargetServiceStub
import jetbrains.buildServer.dotnet.test.dotnet.toolResolvers.ToolResolverStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RunCommandTest {
    @MockK private lateinit var _resultsAnalyzer: ResultsAnalyzer
    @MockK private lateinit var _toolStateWorkflowComposer: ToolStateWorkflowComposer

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }


    @DataProvider
    fun testRunArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                            DotnetConstants.PARAM_PATHS to "path/"),
                        sequenceOf(CommandLineArgument("customArg1")),
                        listOf("customArg1")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "path/"),
                        sequenceOf(CommandLineArgument("--"), CommandLineArgument("customArg1")),
                        listOf("--", "customArg1")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PATHS to "path/"),
                        sequenceOf(CommandLineArgument("   -- "), CommandLineArgument("customArg1")),
                        listOf("   -- ", "customArg1")),
                arrayOf(mapOf(
                            DotnetConstants.PARAM_FRAMEWORK to "dotcore",
                            DotnetConstants.PARAM_CONFIG to "Release"),
                        sequenceOf(CommandLineArgument("--"), CommandLineArgument("customArg1"), CommandLineArgument("customArg2")),
                        listOf("--framework", "dotcore", "--configuration", "Release", "--", "customArg1", "customArg2")),
                arrayOf(mapOf(
                            DotnetConstants.PARAM_FRAMEWORK to "dotcore",
                            DotnetConstants.PARAM_CONFIG to "Release"),
                        sequenceOf(CommandLineArgument("--"), CommandLineArgument("customArg1"), CommandLineArgument("customArg2")),
                        listOf("--framework", "dotcore", "--configuration", "Release", "--", "customArg1", "customArg2")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_FRAMEWORK to "dotcore",
                        DotnetConstants.PARAM_CONFIG to "Release"),
                        sequenceOf(CommandLineArgument(" --  "), CommandLineArgument("customArg1"), CommandLineArgument("customArg2")),
                        listOf("--framework", "dotcore", "--configuration", "Release", " --  ", "customArg1", "customArg2")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_FRAMEWORK to "dotcore",
                        DotnetConstants.PARAM_CONFIG to "Release"),
                        sequenceOf(CommandLineArgument(" --  "), CommandLineArgument("customArg1"), CommandLineArgument("--"), CommandLineArgument("customArg2")),
                        listOf("--framework", "dotcore", "--configuration", "Release", " --  ", "customArg1", "--", "customArg2")),
                arrayOf(mapOf(
                            Pair(DotnetConstants.PARAM_RUNTIME, "win")),
                        sequenceOf(CommandLineArgument("customArg1")),
                        listOf("--runtime", "win", "customArg1")))
    }

    @Test(dataProvider = "testRunArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            customArguments: Sequence<CommandLineArgument>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, targets = sequenceOf("my.csproj"), arguments = customArguments)

        // When
        val actualArguments = command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command)).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("my.csproj") as Any, listOf(listOf("--project", "my.csproj"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                arrayOf(listOf("my.csproj", "my2.csproj") as Any, listOf(listOf("--project", "my.csproj"), listOf("--project", "my2.csproj"))))
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
        Assert.assertEquals(actualCommand, DotnetCommandType.Run)
    }

    @Test
    fun shouldProvideToolExecutableFile() {
        // Given
        val command = createCommand()

        // When
        val actualExecutable = command.toolResolver.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(Path("dotnet")))
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence()): DotnetCommand =
            RunCommand(
                    ParametersServiceStub(parameters),
                    _resultsAnalyzer,
                    TargetServiceStub(targets.map { CommandTarget(Path(it)) }.asSequence()),
                    ArgumentsProviderStub(arguments),
                    ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")), true, _toolStateWorkflowComposer)
            )
}