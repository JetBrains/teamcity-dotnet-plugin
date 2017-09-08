package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.commands.DotCoverInfoProvider
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverInfoProviderTest {
    @DataProvider
    fun testCoverageEnabledData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(mapOf(DotCoverConstants.PARAM_ENABLED to "True"), true),
                arrayOf(mapOf(DotCoverConstants.PARAM_ENABLED to "true"), true),
                arrayOf(mapOf(DotCoverConstants.PARAM_ENABLED to "Fales"), false),
                arrayOf(mapOf(DotCoverConstants.PARAM_ENABLED to "FaLse"), false),
                arrayOf(emptyMap<String, String>(), false))}

    @Test(dataProvider = "testCoverageEnabledData")
    fun shouldDefineCoverageEnabled(
            parameters: Map<String, String>,
            expectedCoverageEnabled: Boolean) {
        // Given
        val instance = createInstance()

        // When
        val actualCoverageEnabled = instance.isCoverageEnabled(parameters)

        // Then
        Assert.assertEquals(actualCoverageEnabled, expectedCoverageEnabled)
    }

    private fun createInstance(): DotCoverInfoProvider {
        return DotCoverInfoProvider()
    }
}