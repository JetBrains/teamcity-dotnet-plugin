package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
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

    private fun create() = RootDotnetCommandTransformer(listOf(_transformerMock1, _transformerMock2, _transformerMock3))
}