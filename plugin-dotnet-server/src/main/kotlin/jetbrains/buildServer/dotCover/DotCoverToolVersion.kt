package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.tools.*

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

    override fun tryGetMatadata(type: ToolMetadataType) = when (_packageId) {
        CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID ->
            "Supports Linux, macOS and Windows with installed .NET Framework 4.6.1+ on an agent. This version is deprecated"

        CoverageConstants.DOTCOVER_PACKAGE_ID -> when (DotCoverToolVersionType.determine(super.getVersion())) {
            DotCoverToolVersionType.UsingDotNetFramework40 -> "Requires .NET Framework 4.0+ installed on an agent"
            DotCoverToolVersionType.UsingDotNetFramework45 -> "Requires .NET Framework 4.5+ installed on an agent"
            DotCoverToolVersionType.UsingDotNetFramework461 -> "Requires .NET Framework 4.6.1+ installed on an agent"
            DotCoverToolVersionType.UsingDotNetFramework472 -> "Requires .NET Framework 4.7.2+ installed on an agent"
            DotCoverToolVersionType.UsingAgentRuntime -> "Requires .NET Core 3.1+ (Linux, macOS) or .NET Framework 4.7.2+ (Windows) installed on an agent"
            else -> null
        }

        else -> null
    }

    private val postfix get(): String = when (_packageId) {
        CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID -> CoverageConstants.DOTCOVER_CROSS_PLATFORM_DEPRECATED_POSTFIX

        CoverageConstants.DOTCOVER_PACKAGE_ID -> when (DotCoverToolVersionType.determine(super.getVersion())) {
            DotCoverToolVersionType.UsingDotNetFramework40 -> CoverageConstants.DOTCOVER_WINDOWS_POSTFIX
            DotCoverToolVersionType.UsingDotNetFramework45 -> CoverageConstants.DOTCOVER_WINDOWS_POSTFIX
            DotCoverToolVersionType.UsingDotNetFramework461 -> CoverageConstants.DOTCOVER_WINDOWS_POSTFIX
            DotCoverToolVersionType.UsingDotNetFramework472 -> CoverageConstants.DOTCOVER_WINDOWS_POSTFIX
            DotCoverToolVersionType.UsingAgentRuntime -> CoverageConstants.DOTCOVER_CROSS_PLATFORM_POSTFIX
            else -> CoverageConstants.DOTCOVER_POSTFIX
        }

        else -> CoverageConstants.DOTCOVER_POSTFIX
    }
}

