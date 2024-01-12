

package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.serviceMessages.ImportDataServiceMessage
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ImportDataServiceMessageTest {
    @DataProvider(name = "serviceMessageCases")
    fun serviceMessageCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("dotNetCoverageDotnetRunner", "dotcover", Path("dotCoverHome"), "##teamcity[importData type='dotNetCoverageDotnetRunner' tool='dotcover' path='dotCoverHome']"),
                arrayOf("dotNetCoverageDotnetRunner", "dotCover", Path("dotCover Home"), "##teamcity[importData type='dotNetCoverageDotnetRunner' tool='dotCover' path='dotCover Home']"),
                arrayOf("dotNetCoverageDotnetRunner", "", Path(""), "##teamcity[importData type='dotNetCoverageDotnetRunner']"))
    }

    @Test(dataProvider = "serviceMessageCases")
    fun shouldProduceServiceMessage(dataProcessorType: String, coverageToolName: String, artifactPath: Path, expectedMessage: String) {
        // Given
        val serviceMessage = ImportDataServiceMessage(dataProcessorType, artifactPath, coverageToolName)

        // When
        val actualMessage = serviceMessage.toString()

        // Then
        Assert.assertEquals(actualMessage, expectedMessage)
    }
}