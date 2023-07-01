package jetbrains.buildServer.dotnet.test.dotnet.commands.resolution.resolvers.transformation

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.resolvers.transformation.TestClassTestsSplittingCommandsResolver
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestClassTestsSplittingCommandsResolverTest {
    @MockK private lateinit var _testsSplittingSettingsMock: TestsSplittingSettings
    @MockK private lateinit var _loggerServiceMock: LoggerService
    @MockK private lateinit var _testCommandMock: DotnetCommand
    @MockK private lateinit var _otherCommandMock: DotnetCommand

    private lateinit var resolver: TestClassTestsSplittingCommandsResolver

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        resolver = TestClassTestsSplittingCommandsResolver(_testsSplittingSettingsMock, _loggerServiceMock)
        justRun { _loggerServiceMock.writeTrace(any()) }
    }

    @Test
    fun `should be on Transformation stage`() {
        // arrange, act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsResolvingStage.Transformation)
    }

    @Test
    fun `should keep test command and write trace message when mode is TestClassNameFilter`() {
        // arrange
        every { _testsSplittingSettingsMock.mode } returns TestsSplittingMode.TestClassNameFilter
        every { _testCommandMock.commandType } returns DotnetCommandType.Test
        every { _loggerServiceMock.writeTrace(any()) } returns Unit

        // act
        val commands = sequenceOf(_testCommandMock)
        val result = resolver.resolve(commands).toList()

        // assert
        assert(result == listOf(_testCommandMock))
        verify { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun `should keep other commands as is when mode is TestClassNameFilter`() {
        // arrange
        every { _testsSplittingSettingsMock.mode } returns TestsSplittingMode.TestClassNameFilter
        every { _otherCommandMock.commandType } returns DotnetCommandType.Build

        // act
        val commands = sequenceOf(_otherCommandMock)
        val result = resolver.resolve(commands).toList()

        // assert
        assert(result == listOf(_otherCommandMock))
        verify(exactly = 0) { _loggerServiceMock.writeTrace(any()) }
    }

    @Test
    fun `should not write trace message when mode is not TestClassNameFilter`() {
        // arrange
        every { _testsSplittingSettingsMock.mode } returns TestsSplittingMode.TestClassNameFilter
        every { _testCommandMock.commandType } returns DotnetCommandType.Test

        // act
        val commands = sequenceOf(_testCommandMock)
        val result = resolver.resolve(commands).toList()

        // assert
        assert(result == listOf(_testCommandMock))
        verify(exactly = 1) { _loggerServiceMock.writeTrace(any()) }
    }
}
