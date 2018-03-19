package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.*
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
    fun isSuccessful(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(0, false, true, true),
                arrayOf(0, true, true, true),
                arrayOf(1, true, true, true),
                arrayOf(1, false, true, false),
                arrayOf(99, true, true, true),
                arrayOf(99, false, true, false),
                arrayOf(-1, true, true, false),
                arrayOf(-1, false, true, false),
                arrayOf(-99, true, true, false),
                arrayOf(-99, false, true, false),

                arrayOf(0, false, false, true),
                arrayOf(0, true, false, true),
                arrayOf(1, true, false, true),
                arrayOf(1, false, false, false),
                arrayOf(99, true, false, true),
                arrayOf(99, false, false, false),
                arrayOf(-1, true, false, true),
                arrayOf(-1, false, false, true),
                arrayOf(-99, true, false, true),
                arrayOf(-99, false, false, true))
    }

    @Test(dataProvider = "isSuccessful")
    fun shouldImplementIsSuccessful(exitCode: Int, hasFailedTest: Boolean, failBuildOnExitCode: Boolean, expectedResult: Boolean) {
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
        val actualResult = testsResultsAnalyzer.isSuccessful(CommandLineResult(sequenceOf(exitCode), lines, emptySequence()))

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}