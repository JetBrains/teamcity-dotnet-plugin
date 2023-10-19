package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.util.VersionComparatorUtil

enum class DotCoverToolVersionType {
    // Deprecated cross-platform version (with bundled runtime)
    UsingBundledRuntime,

    // Cross-platform version
    // requires agent pre-installed .NET Core 3.1+ on Linux/macOS or .NET Framework 4.7.2+ on Windows
    UsingAgentRuntime,

    // Windows-only .NET Framework 4.7.2+ compatible version
    UsingDotNetFramework472,

    // Windows-only .NET Framework 4.6.1+ compatible version
    UsingDotNetFramework461,

    // Windows-only .NET Framework 4.0+ compatible version
    UsingDotNetFramework45,

    // Windows-only .NET Framework 4.0+ compatible version
    UsingDotNetFramework40,

    Unknown;

    companion object {
        private const val DOTCOVER_NET_FRAMEWORK_45_VERSION = "2016.3"
        private const val DOTCOVER_NET_FRAMEWORK_461_VERSION = "2018.2"
        private const val DOTCOVER_NET_FRAMEWORK_472_VERSION = "2021.2"
        private const val DOTCOVER_FIRST_CROSS_PLATFORM_VERSION = "2023.3"

        // works for both versions with or without postfix
        fun determine(version: String) : DotCoverToolVersionType = when {
            !isValid(version) -> Unknown

            version.endsWith(CoverageConstants.DOTCOVER_CROSS_PLATFORM_DEPRECATED_POSTFIX, true) ->
                UsingBundledRuntime

            versionGreaterOrEqualThan(version, DOTCOVER_FIRST_CROSS_PLATFORM_VERSION) ->
                UsingAgentRuntime

            versionBetween(version, DOTCOVER_NET_FRAMEWORK_472_VERSION, DOTCOVER_FIRST_CROSS_PLATFORM_VERSION) ->
                UsingDotNetFramework472

            versionBetween(version, DOTCOVER_NET_FRAMEWORK_461_VERSION, DOTCOVER_NET_FRAMEWORK_472_VERSION) ->
                UsingDotNetFramework461

            versionBetween(version, DOTCOVER_NET_FRAMEWORK_45_VERSION, DOTCOVER_NET_FRAMEWORK_461_VERSION) ->
                UsingDotNetFramework45

            versionLessThan(version, DOTCOVER_NET_FRAMEWORK_45_VERSION) ->
                UsingDotNetFramework40

            else -> Unknown
        }

        private fun versionLessThan(version1: String, version2: String) =
            VersionComparatorUtil.compare(version1, version2) < 0

        private fun versionGreaterOrEqualThan(version1: String, version2: String) =
            VersionComparatorUtil.compare(version1, version2) >= 0

        private fun versionBetween(version: String, version1: String, version2: String) =
            versionGreaterOrEqualThan(version, version1) && versionLessThan(version, version2)

        private fun isValid(version: String) =
            // validates first part of versions like `2023.3.0 Windows` to be a valid version
            version.split(' ').firstOrNull()?.let { Version.tryParse(it) != null } ?: false
    }
}