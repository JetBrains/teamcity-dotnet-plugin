package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import java.util.stream.Stream

class DotCoverPackageIdResolver {

    fun resolvePackageId(toolPackageName: String): String? {
        return Stream.of(
            CoverageConstants.DOTCOVER_PACKAGE_ID,
            CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID
        )
            .filter { toolPackageName.contains(it, ignoreCase = true) }
            .findFirst()
            .orElse(null)
    }
}