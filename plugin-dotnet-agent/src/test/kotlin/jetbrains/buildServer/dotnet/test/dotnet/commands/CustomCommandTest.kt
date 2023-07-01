package jetbrains.buildServer.dotnet.test.dotnet.commands

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.ResultsAnalyzer
import jetbrains.buildServer.dotnet.commands.CustomCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class CustomCommandTest {
    @MockK private lateinit var parametersService: ParametersService
    @MockK private lateinit var resultsAnalyzer: ResultsAnalyzer
    @MockK private lateinit var toolResolver: DotnetToolResolver
    @MockK private lateinit var targetService: TargetService
    private lateinit var command: CustomCommand

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        command = CustomCommand(parametersService, resultsAnalyzer, toolResolver, targetService)
    }

    @Test
    fun `should return empty sequence when getArguments is called`() {
        // arrange
        val expected = emptySequence<CommandLineArgument>()
        every { targetService.targets } returns emptySequence()
        val context: DotnetBuildContext = mockk()

        // act
        val result = command.getArguments(context)

        // assert
        Assert.assertEquals(result, expected)
    }

    @Test
    fun `should return Custom when commandType is called`() {
        // arrange
        val expected = DotnetCommandType.Custom

        // act
        val result = command.commandType

        // assert
        Assert.assertEquals(result, expected)
    }

    @Test
    fun `should return empty sequence when command is called`() {
        // arrange
        val expected = emptySequence<String>()

        // act
        val result = command.command

        // assert
        Assert.assertEquals(result, expected)
    }
}
