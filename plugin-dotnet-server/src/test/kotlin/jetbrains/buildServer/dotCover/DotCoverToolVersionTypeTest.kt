package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverToolVersionTypeTest {
    @DataProvider(name = "versionProvider")
    fun provideVersions(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(DotCoverToolVersionType.UsingBundledRuntime, "2023.3 ${CoverageConstants.DOTCOVER_CROSS_PLATFORM_DEPRECATED_POSTFIX}"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2024.1"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3 ANYTHING"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3.0"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3.0 ANYTHING"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3.0-eap01"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3.0-eap01 ANYTHING"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3.0-tc01"),
            arrayOf(DotCoverToolVersionType.UsingAgentRuntime, "2023.3.0-tc01 ANYTHING"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework472, "2021.2"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework461, "2021.1"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework461, "2018.2"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework45, "2018.1"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework45, "2016.3"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework40, "2016.2"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework40, "2015.1"),
            arrayOf(DotCoverToolVersionType.UsingDotNetFramework40, "1.0.0.4242"),
            arrayOf(DotCoverToolVersionType.Unknown, ""),
            arrayOf(DotCoverToolVersionType.Unknown, "  "),
            arrayOf(DotCoverToolVersionType.Unknown, " ANYTHING     "),
        )
    }

    @Test(dataProvider = "versionProvider")
    fun `should determine dotCover version type`(expectedResult: DotCoverToolVersionType, version: String) {
        // act
        val result = DotCoverToolVersionType.determine(version)

        // assert
        Assert.assertEquals(result, expectedResult)
    }
}
