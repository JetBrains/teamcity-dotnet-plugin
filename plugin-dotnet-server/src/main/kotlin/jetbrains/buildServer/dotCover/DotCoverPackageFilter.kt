package jetbrains.buildServer.dotCover

import jetbrains.buildServer.NuGetPackage
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID
import jetbrains.buildServer.tools.utils.SemanticVersion
import jetbrains.buildServer.util.filters.Filter

class DotCoverPackageFilter : Filter<NuGetPackage> {

    override fun accept(data: NuGetPackage): Boolean {
        val version = SemanticVersion.valueOf(data.packageVersion) ?: return false

        val isCrossPlatformPackage: Boolean = DOTCOVER_DEPRECATED_PACKAGE_ID.equals(data.packageId, ignoreCase = true)
        val isValidCrossPlatformPackage = isCrossPlatformPackage && version.compareTo(OUR_VALID_CROSS_PLATFORM) >= 0

        return isValidCrossPlatformPackage || !isCrossPlatformPackage
    }

    companion object {
        private val OUR_VALID_CROSS_PLATFORM = SemanticVersion.valueOf("2019.2.3")
    }
}

