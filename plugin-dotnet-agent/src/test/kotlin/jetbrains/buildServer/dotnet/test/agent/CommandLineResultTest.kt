package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.CommandLineResult
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class CommandLineResultTest {
    @DataProvider
    fun testDataIsError(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf(CommandLineResult(0, emptyList(), emptyList()), false),
            arrayOf(CommandLineResult(0, listOf("stdout message"), emptyList()), false),
            arrayOf(CommandLineResult(1, emptyList(), emptyList()), true),
            arrayOf(CommandLineResult(1, listOf("stdout message"), emptyList()), true),
            arrayOf(CommandLineResult(1, emptyList(), listOf("stderr message")), true),
            arrayOf(CommandLineResult(0, emptyList(), listOf("stderr message")), true),
        )
    }

    @Test(dataProvider = "testDataIsError")
    fun shouldCorrectlyIdentifyErrorResults(cmdResult: CommandLineResult, isError: Boolean) {
        // Then
        assertEquals(cmdResult.isError, isError)
    }
}