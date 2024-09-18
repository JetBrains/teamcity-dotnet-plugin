package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation.test

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingFilterType
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingModeProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformationStage
import jetbrains.buildServer.dotnet.commands.transformation.test.RootTestsSplittingCommandsTransformer
import jetbrains.buildServer.dotnet.commands.transformation.test.TestsSplittingCommandTransformer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class RootTestsSplittingCommandsTransformerTest {
    @MockK private lateinit var _loggerServiceMock: LoggerService
    @MockK private lateinit var _testsSplittingSettingsMock: TestsSplittingSettings
    @MockK private lateinit var _testsSplittingModeProviderMock: TestsSplittingModeProvider
    @MockK private lateinit var _commandContextMock: DotnetCommandContext
    @MockK private lateinit var _testCommandMock: DotnetCommand

    @MockK private lateinit var _testSplittingTransformerMock1: TestsSplittingCommandTransformer
    @MockK private lateinit var _testSplittingTransformerMock2: TestsSplittingCommandTransformer

    private lateinit var transformer: RootTestsSplittingCommandsTransformer

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        transformer = create()
        justRun { _loggerServiceMock.writeTrace(any()) }
    }

    @Test
    fun `should be on Transformation stage`() {
        // arrange, act
        val result = transformer.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsTransformationStage.Splitting)
    }

    @Test
    fun `should not apply if split tests disabled`() {
        // arrange
        every { _testCommandMock.commandType } returns DotnetCommandType.Test
        every { _testsSplittingSettingsMock.testsClassesFilePath } returns null
        every { _testSplittingTransformerMock1.mode } returns TestsSplittingMode.TestClassNameFilter
        every { _testSplittingTransformerMock2.mode } returns TestsSplittingMode.Suppression
        every { _commandContextMock.toolVersion } returns Version.Empty
        val transformer = create()

        // act
        val result = transformer.apply(_commandContextMock, sequenceOf(_testCommandMock)).toList()

        // assert
        // test splitting tranformers weren't applied due to a missing testClassesFilePath
        verify(exactly = 0) { _testSplittingTransformerMock1.transform(_testCommandMock) }
        verify(exactly = 0) { _testSplittingTransformerMock2.transform(_testCommandMock) }
        // returns the initial command only as the tranformers weren't applied
        Assert.assertEquals(1, result.count())
        Assert.assertEquals(result.get(0), _testCommandMock)
    }

    @Test
    fun `should not apply if not test command`() {
        // arrange
        every { _testCommandMock.commandType } returns DotnetCommandType.Build
        _testsSplittingSettingsMock.let {
            every { it.filterType } returns TestsSplittingFilterType.Excludes
            every { it.testsClassesFilePath } returns "tests.txt"
        }
        every { _testSplittingTransformerMock1.mode } returns TestsSplittingMode.TestClassNameFilter
        every { _testSplittingTransformerMock2.mode } returns TestsSplittingMode.Suppression
        every { _commandContextMock.toolVersion } returns Version.Empty
        every { _testsSplittingModeProviderMock.getMode(any()) } returns TestsSplittingMode.Suppression
        val transformer = create()

        // act
        val result = transformer.apply(_commandContextMock, sequenceOf(_testCommandMock)).toList()

        // assert
        // test splitting tranformers weren't applied due to inappropriate command type
        verify(exactly = 0) { _testSplittingTransformerMock1.transform(_testCommandMock) }
        verify(exactly = 0) { _testSplittingTransformerMock2.transform(_testCommandMock) }
        // returns the initial command only as the tranformers weren't applied
        Assert.assertEquals(1, result.count())
        Assert.assertEquals(result.get(0), _testCommandMock)
    }

    @Test
    fun `should transform test command using correct test splitting transformer when test classes file found`() {
        // arrange
        _testsSplittingSettingsMock.let {
            every { it.filterType } returns TestsSplittingFilterType.Excludes
            every { it.testsClassesFilePath } returns "tests.txt"
        }
        every { _testSplittingTransformerMock1.mode } returns TestsSplittingMode.TestClassNameFilter
        val resultStream2 = listOf(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        every { _testSplittingTransformerMock2.mode } returns TestsSplittingMode.Suppression
        every { _testSplittingTransformerMock2.transform(any()) } answers { resultStream2.asSequence() }

        every { _testCommandMock.commandType } returns DotnetCommandType.Test
        every { _commandContextMock.toolVersion } returns Version.Empty
        every { _testsSplittingModeProviderMock.getMode(any()) } returns TestsSplittingMode.Suppression
        val transformer = create()

        // act
        val result = transformer.apply(_commandContextMock, sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(2, result.count())
        verify(exactly = 0) { _testSplittingTransformerMock1.transform(_testCommandMock) }
        verify(exactly = 1) { _testSplittingTransformerMock2.transform(_testCommandMock) }
    }

    private fun create() = RootTestsSplittingCommandsTransformer(
        _loggerServiceMock,
        _testsSplittingSettingsMock,
        _testsSplittingModeProviderMock,
        listOf(_testSplittingTransformerMock1, _testSplittingTransformerMock2)
    )
}
