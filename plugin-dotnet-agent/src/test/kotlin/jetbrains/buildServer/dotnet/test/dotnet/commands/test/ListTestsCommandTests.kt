package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.ListTestsCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class ListTestsCommandTests {
    @MockK
    private lateinit var _parametersServiceMock: ParametersService

    @MockK
    private lateinit var _resultsAnalyzerMock: ResultsAnalyzer

    @MockK
    private lateinit var _toolResolverMock: DotnetToolResolver

    @MockK
    private lateinit var _targetServiceMock: TargetService

    @MockK
    private lateinit var _targetArgumentsProviderMock: TargetArgumentsProvider

    @BeforeClass
    fun beforeAll() = MockKAnnotations.init(this)

    @Test
    fun `should get arguments`() {
        // arrange
        val command = createCommand()
        val context = mockk<DotnetBuildContext>()

        // act
        val result = command.getArguments(context).toList()

        // assert
        Assert.assertEquals(result.size, 3)
        val (first, second, third) = Triple(result[0], result[1], result[2])
        Assert.assertEquals(first.value, "--list-tests")
        Assert.assertEquals(first.argumentType, CommandLineArgumentType.Mandatory)
        Assert.assertEquals(second.value, "--")
        Assert.assertEquals(second.argumentType, CommandLineArgumentType.Secondary)
        Assert.assertEquals(third.value, "NUnit.DisplayName=FullName")
        Assert.assertEquals(third.argumentType, CommandLineArgumentType.Secondary)
    }

    @Test
    fun `should provide command type`() {
        // arrange
        val command = createCommand()

        // act
        val result = command.commandType

        // assert
        Assert.assertEquals(result, DotnetCommandType.ListTests)
    }

    @Test
    fun `should provide command words`() {
        // arrange
        val command = createCommand()

        // act
        val result = command.commandWords.toList()

        // assert
        Assert.assertEquals(result.size, 1)
        Assert.assertEquals(result[0], "test")
    }

    @Test
    fun `should provide target arguments`() {
        // arrange
        val commandTargetsMock = mockk<Sequence<CommandTarget>>()
        every { _targetServiceMock.targets } returns commandTargetsMock
        val targetArgumentsMock = mockk<Sequence<TargetArguments>>()
        every { _targetArgumentsProviderMock.getTargetArguments(commandTargetsMock) } returns targetArgumentsMock
        val command = createCommand()

        // act
        val result = command.targetArguments

        // assert
        Assert.assertSame(result, targetArgumentsMock)
        verify (exactly = 1) { _targetServiceMock.targets }
        verify (exactly = 1) { _targetArgumentsProviderMock.getTargetArguments(commandTargetsMock) }
    }

    private fun createCommand() = ListTestsCommand(
        _parametersServiceMock,
        _resultsAnalyzerMock,
        _toolResolverMock,
        _targetServiceMock,
        _targetArgumentsProviderMock,
    )
}