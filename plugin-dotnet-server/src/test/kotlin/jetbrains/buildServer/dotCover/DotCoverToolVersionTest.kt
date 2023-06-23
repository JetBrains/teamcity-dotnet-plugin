package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverToolVersionTest {

    @DataProvider
    fun packageIdProvider(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(CoverageConstants.DOTCOVER_PACKAGE_ID, ""),
            arrayOf(CoverageConstants.DOTCOVER_CROSS_PLATFORM_PACKAGE_ID, "Cross-Platform"),
            arrayOf("unknown package", "")
        )
    }

    @Test(dataProvider = "packageIdProvider")
    fun getPostfix_byAllExistingPackages_equalsToExpectedPostfix(toolPackageId: String, expectedPostfix: String) {
        // Arrange
        val version = DotCoverToolVersion(DotCoverToolTypeAdapter(),"2023.1.1", toolPackageId)

        // Act
        val postfix: String = version.getPostfix(toolPackageId)

        // Assert
        Assert.assertEquals(postfix, expectedPostfix)
    }
}