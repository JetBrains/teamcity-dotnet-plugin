package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.dotcover.ImportDataServiceMessage
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ImportDataServiceMessageTest {
    @DataProvider(name = "serviceMessageCases")
    fun serviceMessageCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("dotcover", File("dotCoverHome"), "##teamcity[importData type='dotNetCoverage' tool='dotcover' path='dotCoverHome']"),
                arrayOf("dotCover", File("dotCover Home"), "##teamcity[importData type='dotNetCoverage' tool='dotCover' path='dotCover Home']"),
                arrayOf("", File(""), "##teamcity[importData type='dotNetCoverage' tool='' path='']"))
    }

    @Test(dataProvider = "serviceMessageCases")
    fun shouldProduceServiceMessage(coverageToolName: String, artifactPath: File, expectedMessage: String) {
        // Given
        val serviceMessage = ImportDataServiceMessage(coverageToolName, artifactPath)

        // When
        val actualMessage = serviceMessage.toString()

        // Then
        Assert.assertEquals(actualMessage, expectedMessage)
    }
}