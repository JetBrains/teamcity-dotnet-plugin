

package jetbrains.buildServer.dotnet.test.dotcover

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.dotcover.DotCoverServiceMessage
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverServiceMessageTest {
    @DataProvider(name = "serviceMessageCases")
    fun serviceMessageCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(Path("dotCoverHome"), "##teamcity[dotNetCoverageDotnetRunner dotcover_home='dotCoverHome']"),
                arrayOf(Path("dotCover Home"), "##teamcity[dotNetCoverageDotnetRunner dotcover_home='dotCover Home']"),
                arrayOf(Path(""), "##teamcity[dotNetCoverageDotnetRunner dotcover_home='']"))
    }

    @Test(dataProvider = "serviceMessageCases")
    fun shouldProduceServiceMessage(dotCoverHomePath: Path, expectedMessage: String) {
        // Given
        val serviceMessage = DotCoverServiceMessage(dotCoverHomePath)

        // When
        val actualMessage = serviceMessage.toString()

        // Then
        Assert.assertEquals(actualMessage, expectedMessage)
    }
}