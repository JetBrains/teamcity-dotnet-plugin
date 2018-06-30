package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.dotcover.DotCoverServiceMessage
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverServiceMessageTest {
    @DataProvider(name = "serviceMessageCases")
    fun serviceMessageCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(File("dotCoverHome"), "##teamcity[dotNetCoverage dotcover_home='dotCoverHome']"),
                arrayOf(File("dotCover Home"), "##teamcity[dotNetCoverage dotcover_home='dotCover Home']"),
                arrayOf(File(""), "##teamcity[dotNetCoverage dotcover_home='']"))
    }

    @Test(dataProvider = "serviceMessageCases")
    fun shouldProduceServiceMessage(dotCoverHomePath: File, expectedMessage: String) {
        // Given
        val serviceMessage = DotCoverServiceMessage(dotCoverHomePath)

        // When
        val actualMessage = serviceMessage.toString()

        // Then
        Assert.assertEquals(actualMessage, expectedMessage)
    }
}