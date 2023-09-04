package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverPackageIdResolverTest {

    private val packageIdResolver: DotCoverPackageIdResolver = DotCoverPackageIdResolver()

    @DataProvider
    fun packageIdProvider(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf("JetBrains.dotCover.CommandLineTools.2023.1.0-eap10.nupkg", CoverageConstants.DOTCOVER_PACKAGE_ID),
            arrayOf("JetBrains.dotCover.CommandLineTools.2020.2.1.nupkg", CoverageConstants.DOTCOVER_PACKAGE_ID),
            arrayOf("JetBrains.dotCover.CommandLineTools.2020.2.1.zip", CoverageConstants.DOTCOVER_PACKAGE_ID),
            arrayOf("JetBrains.dotCover.DotNetCliTool.2022.3.0-eap01.nupkg", CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID),
            arrayOf("JetBrains.dotCover.DotNetCliTool.2022.2.4.nupkg", CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID),
            arrayOf("JetBrains.dotCover.DotNetCliTool.2022.2.4.zip", CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID),
            arrayOf("unknown package", null)
        )
    }

    @Test(dataProvider = "packageIdProvider")
    fun resolvePackageId_byAllExistingPackages_resolvesAsExpected(toolPackageName: String, expectedPackageId: String?) {
        // Act
        val packageId: String? = packageIdResolver.resolvePackageId(toolPackageName)

        // Assert
        Assert.assertEquals(packageId, expectedPackageId)
    }
}