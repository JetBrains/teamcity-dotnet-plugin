package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.dotnet.FailedTestDetectorImpl
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class FailedTestDetectorTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(sequenceOf("${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']"), true),
                arrayOf(sequenceOf("${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), true),
                arrayOf(sequenceOf("    ${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), true),
                arrayOf(sequenceOf("abc    ${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), false),
                arrayOf(sequenceOf("abc${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED} details='aaa']", "xyz"), false),
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
        val failedTestDetector = FailedTestDetectorImpl()

        // When
        val actualHasFailedTest = failedTestDetector.hasFailedTest(CommandLineResult(sequenceOf(0), output, emptySequence()))

        // Then
        Assert.assertEquals(actualHasFailedTest, expectedHasFailedTest)
    }
}