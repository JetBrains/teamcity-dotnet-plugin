package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.tools.SimpleToolVersion
import jetbrains.buildServer.tools.ToolMetadata
import jetbrains.buildServer.tools.ToolMetadataType
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersionIdHelper
import jetbrains.buildServer.util.VersionComparatorUtil

open class DotCoverToolVersion(
    toolType: ToolType,
    version: String,
    private val _packageId: String,
    isBundled: Boolean = false
): SimpleToolVersion(
    toolType,
    version,
    ToolVersionIdHelper.getToolId(
        _packageId,
        if (isBundled) CoverageConstants.BUNDLED_TOOL_VERSION_NAME else version
    )
), ToolMetadata {

    override fun getVersion() =
        sequenceOf(super.getVersion(), postfix)
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    val packageVersion: String get() = super.getVersion()

    val packageId: String get() = _packageId

    override fun getDisplayName() =
        "${super.getType().displayName} ${super.getVersion()} $postfix".trim()

    override fun tryGetMatadata(type: ToolMetadataType) = when {
        usingDotNetFramework40 -> "Requires .NET Framework 4.0+ installed on an agent"
        usingDotNetFramework45 -> "Requires .NET Framework 4.5+ installed on an agent"
        usingDotNetFramework461 -> "Requires .NET Framework 4.6.1+ installed on an agent"
        usingDotNetFramework472 -> "Requires .NET Framework 4.7.2+ installed on an agent"
        usingAgentRuntime -> "Requires .NET Core 3.1+ (Linux, macOS) or .NET Framework 4.7.2+ (Windows) installed on an agent"
        usingBundledRuntime -> "Supports Linux, macOS and Windows with installed .NET Framework 4.6.1+ on an agent. This version is deprecated"
        else -> null
    }

    private val postfix get(): String = when {
        usingDotNetFramework40 || usingDotNetFramework45 || usingDotNetFramework461 || usingDotNetFramework472 ->
            CoverageConstants.DOTCOVER_WINDOWS_ONLY_POSTFIX
        usingAgentRuntime -> CoverageConstants.DOTCOVER_CROSS_PLATFORM_POSTFIX
        usingBundledRuntime -> CoverageConstants.DOTCOVER_CROSS_PLATFORM_DEPRECATED_POSTFIX
        else -> CoverageConstants.DOTCOVER_POSTFIX
    }

    private val usingDotNetFramework40 get() =
        _packageId == CoverageConstants.DOTCOVER_PACKAGE_ID
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_NET_FRAMEWORK_45_PACKAGE_VERSION) < 0

    private val usingDotNetFramework45 get() =
        _packageId == CoverageConstants.DOTCOVER_PACKAGE_ID
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_NET_FRAMEWORK_45_PACKAGE_VERSION) >= 0
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_NET_FRAMEWORK_461_PACKAGE_VERSION) < 0

    private val usingDotNetFramework461 get() =
        _packageId == CoverageConstants.DOTCOVER_PACKAGE_ID
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_NET_FRAMEWORK_461_PACKAGE_VERSION) >= 0
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_NET_FRAMEWORK_472_PACKAGE_VERSION) < 0

    private val usingDotNetFramework472 get() =
        _packageId == CoverageConstants.DOTCOVER_PACKAGE_ID
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_NET_FRAMEWORK_472_PACKAGE_VERSION) >= 0
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_CROSS_PLATFORM_PACKAGE_VERSION) < 0

    private val usingAgentRuntime get() =
        _packageId == CoverageConstants.DOTCOVER_PACKAGE_ID
            && VersionComparatorUtil.compare(super.getVersion(), DOTCOVER_CROSS_PLATFORM_PACKAGE_VERSION) >= 0

    private val usingBundledRuntime get() =
        _packageId == CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID

    companion object {
        const val DOTCOVER_NET_FRAMEWORK_45_PACKAGE_VERSION = "2016.3"
        const val DOTCOVER_NET_FRAMEWORK_461_PACKAGE_VERSION = "2018.2"
        const val DOTCOVER_NET_FRAMEWORK_472_PACKAGE_VERSION = "2021.2"
        const val DOTCOVER_CROSS_PLATFORM_PACKAGE_VERSION = "2023.3"
    }
}
