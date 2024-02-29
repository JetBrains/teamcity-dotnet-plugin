package jetbrains.buildServer.dotCover

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.tools.ToolMetadataType
import jetbrains.buildServer.tools.ToolType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverToolVersionTest {

    private val toolType: ToolType = mockk(relaxed = true)

    @DataProvider(name = "versionDataProvider")
    fun provideVersions(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("2016.2", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2016.3", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2018.1", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2018.2", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2021.1", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2021.2", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2023.2", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2023.2.0", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2023.3-eap01", DOTCOVER_PACKAGE_ID, " Windows"),
            arrayOf("2023.3-tc03", DOTCOVER_PACKAGE_ID, " Cross-Platform"),
            arrayOf("2023.3", DOTCOVER_PACKAGE_ID, " Cross-Platform"),
            arrayOf("2023.3.0", DOTCOVER_PACKAGE_ID, " Cross-Platform"),
            arrayOf("2023.3.1", DOTCOVER_PACKAGE_ID, " Cross-Platform"),
            arrayOf("2024.1", DOTCOVER_PACKAGE_ID, " Cross-Platform"),
            arrayOf("2022.2", DOTCOVER_DEPRECATED_PACKAGE_ID, " Cross-Platform (deprecated)")
        )
    }

    @Test(dataProvider = "versionDataProvider")
    fun `should return correct postfix based on version`(version: String, packageId: String, expectedPostfix: String) {
        // arrange
        val dotCoverToolVersion = createDotCoverToolVersion(version, packageId)

        // act
        val resultVersion = dotCoverToolVersion.version

        // assert
        Assert.assertTrue(resultVersion.endsWith(expectedPostfix))
    }

    @Test
    fun `should return correct display name`() {
        // arrange
        val version = "2023.3"
        every { toolType.displayName } returns "dotCover"
        val dotCoverToolVersion = createDotCoverToolVersion(version, DOTCOVER_PACKAGE_ID)

        // act
        val actualDisplayName = dotCoverToolVersion.displayName

        // assert
        Assert.assertEquals(actualDisplayName, "dotCover 2023.3 Cross-Platform")
    }

    @Test
    fun `should return correct package version`() {
        // arrange
        val version = "2023.3"
        val dotCoverToolVersion = createDotCoverToolVersion(version, DOTCOVER_PACKAGE_ID)

        // act
        val result = dotCoverToolVersion.packageVersion

        // assert
        Assert.assertEquals(result, version)
    }

    @Test
    fun `should return correct package ID`() {
        // arrange
        val dotCoverToolVersion = createDotCoverToolVersion("2023.3", DOTCOVER_PACKAGE_ID)

        // act
        val result = dotCoverToolVersion.packageId

        // assert
        Assert.assertEquals(result, DOTCOVER_PACKAGE_ID)
    }

    @DataProvider(name = "metadataDataProvider")
    fun provideMetadata(): Array<Array<Any?>> {
        return arrayOf(
            // version, expected metadata
            arrayOf("2016.2", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_40_REQUIREMENT_WARN),
            arrayOf("2016.3", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_45_REQUIREMENT_WARN),
            arrayOf("2018.1", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_45_REQUIREMENT_WARN),
            arrayOf("2018.2", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_461_REQUIREMENT_WARN),
            arrayOf("2018.3", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_461_REQUIREMENT_WARN),
            arrayOf("2021.1", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_461_REQUIREMENT_WARN),
            arrayOf("2021.2", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_472_REQUIREMENT_WARN),
            arrayOf("2023.2", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_472_REQUIREMENT_WARN),
            arrayOf("2023.3-eap01", DOTCOVER_PACKAGE_ID, NET_FRAMEWORK_472_REQUIREMENT_WARN),
            arrayOf("2023.3", DOTCOVER_PACKAGE_ID, NET_CORE_AND_FRAMEWORK_REQUIREMENT_WARN),
            arrayOf("2023.3.0", DOTCOVER_PACKAGE_ID, NET_CORE_AND_FRAMEWORK_REQUIREMENT_WARN),
            arrayOf("2023.3.0-tc03", DOTCOVER_PACKAGE_ID, NET_CORE_AND_FRAMEWORK_REQUIREMENT_WARN),
            arrayOf("2024.1", DOTCOVER_PACKAGE_ID, NET_CORE_AND_FRAMEWORK_REQUIREMENT_WARN),
            arrayOf("ANY", DOTCOVER_DEPRECATED_PACKAGE_ID, DEPRECATED_PACKAGE_REQUIREMENT_WARN)
        )
    }

    @Test(dataProvider = "metadataDataProvider")
    fun `should return correct metadata based on version`(version: String, packageId: String, expectedMetadata: String?) {
        // arrange
        val dotCoverToolVersion = createDotCoverToolVersion(version, packageId)

        // act
        val actualMetadata = dotCoverToolVersion.tryGetMatadata(ToolMetadataType.Notes)

        // assert
        Assert.assertEquals(actualMetadata, expectedMetadata)
    }

    private fun createDotCoverToolVersion(version: String, packageId: String): DotCoverToolVersion {
        return DotCoverToolVersion(
            toolType = toolType,
            version = version,
            _packageId = if (version == "deprecated_version") "deprecated" else packageId
        )
    }

    companion object {
        const val DOTCOVER_PACKAGE_ID = "JetBrains.dotCover.CommandLineTools"
        const val DOTCOVER_DEPRECATED_PACKAGE_ID = "JetBrains.dotCover.DotNetCliTool"
        const val NET_FRAMEWORK_40_REQUIREMENT_WARN = "Requires .NET Framework 4.0+ installed on an agent"
        const val NET_FRAMEWORK_45_REQUIREMENT_WARN = "Requires .NET Framework 4.5+ installed on an agent"
        const val NET_FRAMEWORK_461_REQUIREMENT_WARN = "Requires .NET Framework 4.6.1+ installed on an agent"
        const val NET_FRAMEWORK_472_REQUIREMENT_WARN = "Requires .NET Framework 4.7.2+ installed on an agent"
        const val NET_CORE_AND_FRAMEWORK_REQUIREMENT_WARN = "Requires .NET Core 3.1+ (Linux, macOS) or .NET Framework 4.7.2+ (Windows) installed on an agent"
        const val DEPRECATED_PACKAGE_REQUIREMENT_WARN = "Supports Linux, macOS and Windows with installed .NET Framework 4.6.1+ on an agent. This version is deprecated"
    }
}
