

package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformationStage
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformer
import jetbrains.buildServer.dotnet.commands.transformation.RootDotnetCommandTransformer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class RootDotnetCommandTransformerTest {
    @MockK
    private lateinit var _transformerMock1: DotnetCommandsTransformer

    @MockK
    private lateinit var _transformerMock2: DotnetCommandsTransformer

    @MockK
    private lateinit var _transformerMock3: DotnetCommandsTransformer

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should be on initial stage`() {
        // arrange
        val transformer = create()

        // act
        val result = transformer.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsTransformationStage.Initial)
    }

    @Test
    fun `should apply all child transformers`() {
        // arrange
        every { _transformerMock1.stage } answers { DotnetCommandsTransformationStage.Targeting }
        every { _transformerMock2.stage } answers { DotnetCommandsTransformationStage.Transformation }
        every { _transformerMock3.stage } answers { DotnetCommandsTransformationStage.FinalComposition }


        val transformer1ResultStream = mockk<DotnetCommandsStream>()
        every { _transformerMock1.shouldBeApplied(any(), any()) } answers { true }
        every { _transformerMock1.apply(any(), any()) } answers { transformer1ResultStream }
        val transformer2ResultStream = mockk<DotnetCommandsStream>()
        every { _transformerMock2.shouldBeApplied(any(), any()) } answers { true }
        every { _transformerMock2.apply(any(), any()) } answers { transformer2ResultStream }
        val transformer3ResultStream = mockk<DotnetCommandsStream>()
        every { _transformerMock3.shouldBeApplied(any(), any()) } answers { true }
        every { _transformerMock3.apply(any(), any()) } answers { transformer3ResultStream }

        val transformer = create()

        val commandContextMock = mockk<DotnetCommandContext>()
        val initialStream = mockk<DotnetCommandsStream>()

        // act
        val result = transformer.apply(commandContextMock, initialStream)

        // assert
        Assert.assertNotNull(result)
        verify(exactly = 1) { _transformerMock1.apply(match { it == commandContextMock }, match { it == initialStream }) }
        verify(exactly = 1) { _transformerMock2.apply(match { it == commandContextMock }, match { it == transformer1ResultStream }) }
        verify(exactly = 1) { _transformerMock3.apply(match { it == commandContextMock }, match { it == transformer2ResultStream }) }
        Assert.assertEquals(result, transformer3ResultStream)
    }

    @Test
    fun `should skip transformers when they are not applicable`() {
        // arrange
        every { _transformerMock1.shouldBeApplied(any(), any()) } answers { false }
        every { _transformerMock1.stage } answers { DotnetCommandsTransformationStage.Targeting }

        val transformer2ResultStream = mockk<DotnetCommandsStream>()
        every { _transformerMock2.shouldBeApplied(any(), any()) } answers { true }
        every { _transformerMock2.apply(any(), any()) } answers { transformer2ResultStream }
        every { _transformerMock2.stage } answers { DotnetCommandsTransformationStage.Transformation }

        every { _transformerMock3.shouldBeApplied(any(), any()) } answers { false }
        every { _transformerMock3.stage } answers { DotnetCommandsTransformationStage.FinalComposition }

        val transformer = create()

        val contextMock = mockk<DotnetCommandContext>()
        val initialStream = mockk<DotnetCommandsStream>()

        // act
        val result = transformer.apply(contextMock, initialStream)

        // assert
        Assert.assertNotNull(result)
        verify(exactly = 1) { _transformerMock1.shouldBeApplied(match { it == contextMock }, match { it == initialStream }) }
        verify(exactly = 0) { _transformerMock1.apply(any(), any()) }

        verify(exactly = 1) { _transformerMock2.shouldBeApplied(match { it == contextMock }, match { it == initialStream }) }
        verify(exactly = 1) { _transformerMock2.apply(match { it == contextMock }, match { it == initialStream }) }

        verify(exactly = 1) { _transformerMock3.shouldBeApplied(match { it == contextMock }, match { it == transformer2ResultStream }) }
        verify(exactly = 0) { _transformerMock3.apply(any(), any()) }
        Assert.assertEquals(result, transformer2ResultStream)
    }

    private fun create() = RootDotnetCommandTransformer(listOf(_transformerMock1, _transformerMock2, _transformerMock3))
}