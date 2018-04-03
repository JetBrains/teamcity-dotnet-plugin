package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TestsResultsAnalyzerImpl
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.Serializable
import java.util.*

class TestsResultsAnalyzerImplTest {
    private var _ctx: Mockery? = null
    private var _buildOptions: BuildOptions? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _buildOptions = _ctx!!.mock<BuildOptions>(BuildOptions::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(sequenceOf("${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']"), true),
                arrayOf(sequenceOf("${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), true),
                arrayOf(sequenceOf("    ${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), true),
                arrayOf(sequenceOf("abc    ${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), true),
                arrayOf(sequenceOf("abc${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), true),
                arrayOf(sequenceOf("abc", "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), true),
                arrayOf(sequenceOf("abc", "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FINISHED} details='aaa']", "xyz"), false),
                arrayOf(sequenceOf("abc", "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_IGNORED} details='aaa']", "xyz"), false),
                arrayOf(sequenceOf("abc", "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.BLOCK_OPENED} details='aaa']", "xyz"), false),
                arrayOf(sequenceOf("abc", "xyz"), false),
                arrayOf(sequenceOf(""), false))
    }

    @Test(dataProvider = "testData")
    fun shouldImplementCheckSuccess(output: Sequence<String>, expectedHasFailedTest: Boolean) {
        // Given
        val failedTestDetector = TestsResultsAnalyzerImpl(_buildOptions!!)

        // When
        val actualHasFailedTest = output.map { failedTestDetector.hasFailedTest(it) }.filter { it }.any()

        // Then
        Assert.assertEquals(actualHasFailedTest, expectedHasFailedTest)
    }

    @DataProvider
    fun checkAnalyzeResult(): Array<Array<Serializable>> {
        return arrayOf(
                arrayOf(0, false, true, EnumSet.of(CommandResult.Success)),
                arrayOf(0, true, true, EnumSet.of(CommandResult.Success)),
                arrayOf(1, true, true, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(1, false, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(99, true, true, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(99, false, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-1, true, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-1, false, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-99, true, true, EnumSet.of(CommandResult.Fail)),
                arrayOf(-99, false, true, EnumSet.of(CommandResult.Fail)),

                arrayOf(0, false, false, EnumSet.of(CommandResult.Success)),
                arrayOf(0, true, false, EnumSet.of(CommandResult.Success)),
                arrayOf(1, true, false, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(1, false, false, EnumSet.of(CommandResult.Fail)),
                arrayOf(99, true, false, EnumSet.of(CommandResult.Success, CommandResult.FailedTests)),
                arrayOf(99, false, false, EnumSet.of(CommandResult.Fail)),
                arrayOf(-1, true, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-1, false, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-99, true, false, EnumSet.of(CommandResult.Success)),
                arrayOf(-99, false, false, EnumSet.of(CommandResult.Success)))
    }

    @Test(dataProvider = "checkAnalyzeResult")
    fun shouldAnalyzeResult(exitCode: Int, hasFailedTest: Boolean, failBuildOnExitCode: Boolean, expectedResult: EnumSet<CommandResult>) {
        // Given
        val testsResultsAnalyzer = TestsResultsAnalyzerImpl(_buildOptions!!)
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<BuildOptions>(_buildOptions).failBuildOnExitCode
                will(returnValue(failBuildOnExitCode))
            }
        })

        var lines: Sequence<String>;
        if (hasFailedTest) {
            lines = sequenceOf("some line", TestsResultsAnalyzerImpl.FailedTestMarker)
        }
        else {
            lines = sequenceOf("some line")
        }

        // When
        val actualResult = testsResultsAnalyzer.analyze(CommandLineResult(sequenceOf(exitCode), lines, emptySequence()))

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}