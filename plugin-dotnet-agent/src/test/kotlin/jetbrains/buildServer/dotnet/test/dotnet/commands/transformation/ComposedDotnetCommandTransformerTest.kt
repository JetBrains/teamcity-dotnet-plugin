

package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.ToolResolver
import jetbrains.buildServer.dotnet.commands.transformation.ComposedDotnetCommandTransformer
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformationStage
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ComposedDotnetCommandTransformerTest {
    @BeforeMethod
    fun setup() {
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should be on FinalComposition stage`() {
        // arrange
        val transformer = create()

        // act
        val result = transformer.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsTransformationStage.FinalComposition)
    }

    @Test
    fun `should resolve every command in composed command`() {
        // arrange
        val (toolResolverMock1, toolResolverMock2) = Pair(mockk<ToolResolver>(), mockk<ToolResolver>())
        every { toolResolverMock1.isCommandRequired } answers { true }
        every { toolResolverMock2.isCommandRequired } answers { false }

        val (commandLineArgumentsMock1, commandLineArgumentsMock2, commandLineArgumentsMock3) = Triple(mockk<CommandLineArgument>(), mockk<CommandLineArgument>(), mockk<CommandLineArgument>())
        val (targetArgumentsMock1, targetArgumentsMock2, targetArgumentsMock3) = Triple(mockk<TargetArguments>(), mockk<TargetArguments>(), mockk<TargetArguments>())
        every { targetArgumentsMock1.arguments } answers { sequenceOf(commandLineArgumentsMock1, commandLineArgumentsMock2) }
        every { targetArgumentsMock2.arguments } answers { sequenceOf(commandLineArgumentsMock2, commandLineArgumentsMock3) }
        every { targetArgumentsMock3.arguments } answers { sequenceOf(commandLineArgumentsMock1, commandLineArgumentsMock3) }

        val (commandSpecificArgMock1, commandSpecificArgMock2, commandSpecificArgMock3) = Triple(mockk<CommandLineArgument>(), mockk<CommandLineArgument>(), mockk<CommandLineArgument>())

        val (commandMock1, commandMock2, commandMock3) = Triple(mockk<DotnetCommand>(), mockk<DotnetCommand>(), mockk<DotnetCommand>())
        every { commandMock1.toolResolver } answers { toolResolverMock1 }
        every { commandMock2.toolResolver } answers { toolResolverMock1 }
        every { commandMock3.toolResolver } answers { toolResolverMock2 }
        every { commandMock1.command } answers { sequenceOf("build") }
        every { commandMock2.command } answers { sequenceOf("nuget", "restore") }
        every { commandMock1.targetArguments } answers { sequenceOf(targetArgumentsMock1) }
        every { commandMock2.targetArguments } answers { sequenceOf(targetArgumentsMock2) }
        every { commandMock3.targetArguments } answers { sequenceOf(targetArgumentsMock3) }
        every { commandMock1.getArguments(any()) } answers { sequenceOf(commandSpecificArgMock1, commandSpecificArgMock2) }
        every { commandMock2.getArguments(any()) } answers { sequenceOf(commandSpecificArgMock2, commandSpecificArgMock3) }
        every { commandMock3.getArguments(any()) } answers { sequenceOf(commandSpecificArgMock1, commandSpecificArgMock3) }
        val transformer = create()

        // act
        val result = transformer.apply(mockk<DotnetCommandContext>(), sequenceOf(commandMock1, commandMock2, commandMock3)).toList()

        // assert
        val (composedCommand1, composedCommand2, composedCommand3) = Triple(result[0], result[1], result[2])
        val composedCommand1Args = composedCommand1.getArguments(mockk()).toList()
        Assert.assertEquals(composedCommand1Args[0].argumentType, CommandLineArgumentType.Mandatory)
        Assert.assertEquals(composedCommand1Args[0].value, "build")
        Assert.assertSame(composedCommand1Args[1], commandLineArgumentsMock1)
        Assert.assertSame(composedCommand1Args[2], commandLineArgumentsMock2)
        Assert.assertSame(composedCommand1Args[3], commandSpecificArgMock1)
        Assert.assertSame(composedCommand1Args[4], commandSpecificArgMock2)

        val composedCommand2Args = composedCommand2.getArguments(mockk()).toList()
        Assert.assertEquals(composedCommand2Args[0].argumentType, CommandLineArgumentType.Mandatory)
        Assert.assertEquals(composedCommand2Args[0].value, "nuget")
        Assert.assertEquals(composedCommand2Args[1].argumentType, CommandLineArgumentType.Mandatory)
        Assert.assertEquals(composedCommand2Args[1].value, "restore")
        Assert.assertSame(composedCommand2Args[2], commandLineArgumentsMock2)
        Assert.assertSame(composedCommand2Args[3], commandLineArgumentsMock3)
        Assert.assertSame(composedCommand2Args[4], commandSpecificArgMock2)
        Assert.assertSame(composedCommand2Args[5], commandSpecificArgMock3)

        val composedCommand3Args = composedCommand3.getArguments(mockk()).toList()
        Assert.assertSame(composedCommand3Args[0], commandLineArgumentsMock1)
        Assert.assertSame(composedCommand3Args[1], commandLineArgumentsMock3)
        Assert.assertSame(composedCommand3Args[2], commandSpecificArgMock1)
        Assert.assertSame(composedCommand3Args[3], commandSpecificArgMock3)
    }

    private fun create() = ComposedDotnetCommandTransformer()
}