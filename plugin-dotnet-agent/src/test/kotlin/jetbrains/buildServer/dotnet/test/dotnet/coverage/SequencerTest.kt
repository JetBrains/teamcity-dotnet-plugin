package jetbrains.buildServer.dotnet.test.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.SequencerImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class SequencerTest {

    @DataProvider
    fun getSequence(): Array<Array<Any>> = arrayOf(
            arrayOf("", "1", false),
            arrayOf("  ", "1", false),
            arrayOf("1", "2", false),
            arrayOf("12", "13", false),
            arrayOf("123456", "123457", false),
            arrayOf("a", "", true),
            arrayOf("123a", "", true),
            arrayOf("a123", "", true)
        )

    @Test(dataProvider = "getSequence")
    fun shouldGenerateSequence(
        baseValue: String,
        expectedValue: String,
        expectedThrowsIllegalArgumentException: Boolean
    ) {
        // arrange
        val sequencer = SequencerImpl()
        var actualThrowsIllegalArgumentException = false
        var actualValue: String? = null

        // act
        try {
            actualValue = sequencer.nextFrom(baseValue)
        } catch (ex: IllegalArgumentException) {
            actualThrowsIllegalArgumentException = true
        }

        // assert
        Assert.assertEquals(actualThrowsIllegalArgumentException, expectedThrowsIllegalArgumentException)
        if (!expectedThrowsIllegalArgumentException) {
            Assert.assertEquals(actualValue, expectedValue)
        }
    }
}
