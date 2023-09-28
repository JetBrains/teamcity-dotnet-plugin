package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation.test

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.transformation.test.TestClassNameFilterTestSplittingCommandTransformer
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestClassNameFilterTestsSplittingCommandTransformerTest {
    @MockK private lateinit var _loggerServiceMock: LoggerService
    @MockK private lateinit var _testCommandMock: DotnetCommand

    private lateinit var transformer: TestClassNameFilterTestSplittingCommandTransformer

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        transformer = TestClassNameFilterTestSplittingCommandTransformer(_loggerServiceMock)
        justRun { _loggerServiceMock.writeTrace(any()) }
    }

    @Test
    fun `should keep test command and write trace message`() {
        // arrange
        every { _testCommandMock.commandType } returns DotnetCommandType.Test
        every { _loggerServiceMock.writeTrace(any()) } returns Unit

        // act
        val result = transformer.transform(_testCommandMock).toList()

        // assert
        assert(result == listOf(_testCommandMock))
        verify { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }
}
