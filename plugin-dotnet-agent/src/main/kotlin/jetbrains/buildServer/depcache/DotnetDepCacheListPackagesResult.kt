package jetbrains.buildServer.depcache

data class DotnetDepCacheListPackagesResult(
    val version: Int?,
    val parameters: String?,
    val problems: List<Problem>?,
    val projects: List<Project>?
)

data class Problem(
    val level: String?,
    val text: String?
)

data class Project(
    val path: String?,
    val frameworks: List<Framework>?
)

data class Framework(
    val framework: String?,
    val topLevelPackages: List<Package>?,
    val transitivePackages: List<Package>?
)

data class Package(
    val id: String?,
    val requestedVersion: String? = null,
    val resolvedVersion: String?,
    val autoReferenced: Boolean? = false
) {

    val packageCompositeName: String?
        get() = id?.let { packageId ->
            resolvedVersion?.let { version ->
                return "$packageId:$version"
            }
        }
}