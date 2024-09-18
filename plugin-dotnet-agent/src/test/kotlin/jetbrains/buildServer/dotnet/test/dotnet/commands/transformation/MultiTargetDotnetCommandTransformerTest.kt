package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformationStage
import jetbrains.buildServer.dotnet.commands.transformation.MultiTargetDotnetCommandTransformer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class MultiTargetDotnetCommandTransformerTest {
    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should be on Targeting stage`() {
        // arrange
        val transformer = create()

        // act
        val result = transformer.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsTransformationStage.Targeting)
    }

    @Test
    fun `should resolve one command to multiple if it has target arguments more then 1`() {
        // arrange
        val (commandMock1, commandMock2) = Pair(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        val (targetArgumentsMock1, targetArgumentsMock2) = Pair(mockk<TargetArguments>(), mockk<TargetArguments>())
        val (commandLineArgsMock1, commandLineArgsMock2) = Pair(mockk<Sequence<CommandLineArgument>>(), mockk<Sequence<CommandLineArgument>>())
        every { commandMock1.targetArguments } answers { sequenceOf(targetArgumentsMock1) }
        every { commandMock1.getArguments(any()) } answers { commandLineArgsMock1 }
        every { commandMock2.targetArguments } answers { sequenceOf(targetArgumentsMock1, targetArgumentsMock2) }
        every { commandMock2.getArguments(any()) } answers { commandLineArgsMock2 }
        val transformer = create()
        val commands = sequenceOf(commandMock1, commandMock2)

        // act
        val result = transformer.apply(mockk<DotnetCommandContext>(), commands).toList()

        // assert
        Assert.assertEquals(result.size, 3)
        result.forEach {
            Assert.assertTrue(it is MultiTargetDotnetCommandTransformer.SpecificTargetDotnetCommand)
            Assert.assertEquals(it.targetArguments.count(), 1)
        }
        Assert.assertEquals(result[0].targetArguments.first(), targetArgumentsMock1)
        Assert.assertEquals(result[0].getArguments(mockk()), commandLineArgsMock1)
        Assert.assertEquals(result[1].targetArguments.first(), targetArgumentsMock1)
        Assert.assertEquals(result[1].getArguments(mockk()), commandLineArgsMock2)
        Assert.assertEquals(result[2].targetArguments.first(), targetArgumentsMock2)
        Assert.assertEquals(result[2].getArguments(mockk()), commandLineArgsMock2)
    }

    @Test
    fun `should resolve even if target arguments of original command are empty`() {
        // arrange
        val (commandMock1, commandMock2) = Pair(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        val (targetArgumentsMock1, targetArgumentsMock2) = Pair(mockk<TargetArguments>(), mockk<TargetArguments>())
        val (commandLineArgsMock1, commandLineArgsMock2) = Pair(mockk<Sequence<CommandLineArgument>>(), mockk<Sequence<CommandLineArgument>>())
        every { commandMock1.targetArguments } answers { emptySequence() }
        every { commandMock1.getArguments(any()) } answers { commandLineArgsMock1 }
        every { commandMock2.targetArguments } answers { sequenceOf(targetArgumentsMock1, targetArgumentsMock2) }
        every { commandMock2.getArguments(any()) } answers { commandLineArgsMock2 }
        val transformer = create()

        // act
        val result = transformer.apply(mockk<DotnetCommandContext>(), sequenceOf(commandMock1, commandMock2)).toList()

        // assert
        Assert.assertEquals(result.size, 3)
        result.forEach {
            Assert.assertTrue(it is MultiTargetDotnetCommandTransformer.SpecificTargetDotnetCommand)
            Assert.assertEquals(it.targetArguments.count(), 1)
        }
        Assert.assertEquals(result[0].targetArguments.first().arguments.count(), 0)
        Assert.assertEquals(result[0].getArguments(mockk()), commandLineArgsMock1)
        Assert.assertEquals(result[1].targetArguments.first(), targetArgumentsMock1)
        Assert.assertEquals(result[1].getArguments(mockk()), commandLineArgsMock2)
        Assert.assertEquals(result[2].targetArguments.first(), targetArgumentsMock2)
        Assert.assertEquals(result[2].getArguments(mockk()), commandLineArgsMock2)
    }

    private fun create() = MultiTargetDotnetCommandTransformer()
}