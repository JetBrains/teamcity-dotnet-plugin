package jetbrains.buildServer.dotnet

// https://learn.microsoft.com/en-us/nuget/concepts/package-versioning?tabs=semver20sort#where-nugetversion-diverges-from-semantic-versioning
data class NuGetPackageVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val revision: Int,
    val preRelease: String = ""
) {
    constructor(
        major: Int,
        minor: Int,
        patch: Int,
        preRelease: String = ""
    ) : this(major, minor, patch, 0, preRelease)

    override fun toString() = buildString {
        append("$major.$minor.$patch")
        if (revision != 0) append(".$revision")
        if (preRelease.isNotEmpty()) append("-$preRelease")
    }
}