

package jetbrains.buildServer.dotnet.test.dotnet.commands

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.NugetDeleteCommand
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.dotnet.test.dotnet.ArgumentsProviderStub
import jetbrains.buildServer.dotnet.test.dotnet.toolResolvers.ToolResolverStub
import jetbrains.buildServer.rx.Observer
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NugetDeleteCommandTest {
    private lateinit var _ctx: Mockery
    @MockK private lateinit var _resultsAnalyzer: ResultsAnalyzer
    @MockK private lateinit var _resultsObserver: Observer<CommandResultEvent>
    @MockK private lateinit var _toolStateWorkflowComposer: ToolStateWorkflowComposer

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testNugetDeleteArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_NUGET_PACKAGE_ID to "id version",
                        DotnetConstants.PARAM_NUGET_API_KEY to "key",
                        DotnetConstants.PARAM_NUGET_PACKAGE_SOURCE to "http://jb.com"),
                        listOf("id", "version", "--api-key", "key",
                                "--source", "http://jb.com", "--non-interactive", "--force-english-output", "customArg1"))
        )
    }

    @Test(dataProvider = "testNugetDeleteArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        val actualArguments = command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command)).map { it.value }.toList()

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
        Assert.assertEquals(actualCommand, DotnetCommandType.NuGetDelete)
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
            arguments: Sequence<CommandLineArgument> = emptySequence()): DotnetCommand =
            NugetDeleteCommand(
                    ParametersServiceStub(parameters),
                    _resultsAnalyzer,
                    ArgumentsProviderStub(arguments),
                    ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")),true, _toolStateWorkflowComposer),
                    _resultsObserver)
}