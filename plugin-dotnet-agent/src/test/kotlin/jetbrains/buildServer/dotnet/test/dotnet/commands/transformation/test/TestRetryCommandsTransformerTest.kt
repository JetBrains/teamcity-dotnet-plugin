package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation.test

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryReportReader
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryFilterProvider
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetrySettings
import jetbrains.buildServer.dotnet.commands.transformation.test.TestRetryCommandsTransformer
import jetbrains.buildServer.rx.emptyDisposable
import jetbrains.buildServer.rx.toDisposable
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestRetryCommandsTransformerTest {
    @MockK
    private lateinit var _loggerServiceMock: LoggerService

    @MockK
    private lateinit var _retrySettingsMock: TestRetrySettings

    @MockK
    private lateinit var _retryFilterMock: TestRetryFilterProvider

    @MockK
    private lateinit var _retryReportReaderMock: TestRetryReportReader

    @MockK
    private lateinit var _commandContextMock: DotnetCommandContext

    @MockK
    private lateinit var _testCommandMock: DotnetCommand

    private lateinit var _transformer: TestRetryCommandsTransformer

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        justRun { _loggerServiceMock.writeMessage(any()) }
        justRun { _retryReportReaderMock.cleanup() }
        every { _retryFilterMock.setTestNames(any()) } returns emptyDisposable()

        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.environmentBuilders } returns emptyList()
            every { it.title } returns ""
        }

        _transformer = TestRetryCommandsTransformer(
            _loggerServiceMock,
            _retrySettingsMock,
            _retryFilterMock,
            _retryReportReaderMock
        )
    }

    @Test
    fun `should transform to 3 test retry commands when tests pass after second retry`() {
        // arrange
        _retrySettingsMock.let {
            every { it.maxRetries } returns 5
            every { it.maxFailures } returns 10
        }

        every { _retryReportReaderMock.readFailedTestNames() } returnsMany listOf(
            listOf("FailedTest1", "FailedTest2"),
            listOf("FailedTest1"),
            emptyList()
        )

        // act
        val retryTestCommands = _transformer.apply(_commandContextMock, sequenceOf(_testCommandMock))
            .map { it.also { it.environmentBuilders.map { it.build(_commandContextMock) }.toDisposable().dispose() } }
            .toList()

        // assert
        Assert.assertEquals(retryTestCommands.size, 3)
        Assert.assertEquals(retryTestCommands[0].title, "")
        Assert.assertEquals(retryTestCommands[1].title, "dotnet test retry #1 (2 tests)")
        Assert.assertEquals(retryTestCommands[2].title, "dotnet test retry #2 (1 tests)")
        _retryFilterMock.let {
            verify(exactly = 1) { it.setTestNames(listOf("FailedTest1", "FailedTest2")) }
            verify(exactly = 1) { it.setTestNames(listOf("FailedTest1")) }
            verify(exactly = 1) { it.setTestNames(emptyList()) }
            confirmVerified(it)
        }
        verify(exactly = 2 * 3) { _retryReportReaderMock.cleanup() }
    }

    @Test
    fun `should transform to max retries count + 1 commands when max retry attempts are exceeded`() {
        // arrange
        val retries = 10
        _retrySettingsMock.let {
            every { it.maxRetries } returns retries
            every { it.maxFailures } returns 10
        }

        every { _retryReportReaderMock.readFailedTestNames() } returns listOf("SameAlwaysFailingTest")

        // act
        val retryTestCommands = _transformer.apply(_commandContextMock, sequenceOf(_testCommandMock))
            .map { it.also { it.environmentBuilders.map { it.build(_commandContextMock) }.toDisposable().dispose() } }
            .toList()

        // assert
        Assert.assertEquals(retryTestCommands.size, retries + 1)
        _retryFilterMock.let {
            verify(exactly = 1) { it.setTestNames(emptyList()) }
            verify(exactly = retries) { it.setTestNames(listOf("SameAlwaysFailingTest")) }
            confirmVerified(it)
        }
        verify(exactly = 2 * (retries + 1)) { _retryReportReaderMock.cleanup() }
    }

    @Test
    fun `should transform to initial command when too many tests has failed`() {
        // arrange
        val failedTestsCount = 1000
        _retrySettingsMock.let {
            every { it.maxRetries } returns 10
            every { it.maxFailures } returns failedTestsCount
        }

        every { _retryReportReaderMock.readFailedTestNames() } returns MutableList(failedTestsCount) { "FailedTest$it" }

        // act
        val retryTestCommands = _transformer.apply(_commandContextMock, sequenceOf(_testCommandMock))
            .map { it.also { it.environmentBuilders.map { it.build(_commandContextMock) }.toDisposable().dispose() } }
            .toList()

        // assert
        Assert.assertEquals(retryTestCommands.size, 1)
        _retryFilterMock.let {
            verify(exactly = 1) { it.setTestNames(emptyList()) }
            confirmVerified(it)
        }
        verify(exactly = 2) { _retryReportReaderMock.cleanup() }
    }
}