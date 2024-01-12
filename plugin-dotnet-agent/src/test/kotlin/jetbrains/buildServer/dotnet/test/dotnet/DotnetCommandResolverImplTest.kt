

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandResolverImpl
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class DotnetCommandResolverImplTest {
    @MockK
    private lateinit var _dotnetCommandMock1: DotnetCommand

    @MockK
    private lateinit var _dotnetCommandMock2: DotnetCommand

    @MockK
    private lateinit var _dotnetCommandMock3: DotnetCommand

    @MockK
    private lateinit var _parametersServiceMock: ParametersService

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        every { _dotnetCommandMock1.commandType } answers { DotnetCommandType.Build }
        every { _dotnetCommandMock2.commandType } answers { DotnetCommandType.Test }
        every { _dotnetCommandMock3.commandType } answers { DotnetCommandType.Restore }
    }

    @Test
    fun `should resolve command by valid command id from parameter`() {
        // arrange
        val buildCommandId = DotnetCommandType.Build.id
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { buildCommandId }
        val resolver = create()

        // act
        val result = resolver.command

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result, _dotnetCommandMock1)
        verify (exactly = 1) { _parametersServiceMock.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND) }
    }

    @Test
    fun `should not resolve command by invalid command id from parameter`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { "INVALID" }
        val resolver = create()

        // act
        val result = resolver.command

        // assert
        Assert.assertNull(result)
        verify (exactly = 1) { _parametersServiceMock.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND) }
    }

    private fun create() =
        DotnetCommandResolverImpl(
            _parametersServiceMock,
            listOf(_dotnetCommandMock1, _dotnetCommandMock2, _dotnetCommandMock3),
        )
}