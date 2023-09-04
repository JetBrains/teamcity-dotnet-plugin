package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.tools.SimpleToolVersion
import jetbrains.buildServer.tools.ToolMetadata
import jetbrains.buildServer.tools.ToolMetadataType
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersionIdHelper

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
        usingDotNetFramework -> "Supports only Windows with installed .NET Framework 4.6.1+"
        usingAgentRuntime -> "Requires .NET Core 3.1+ (Linux, macOS) or .NET Framework 4.6.1+"
        usingBundledRuntime -> "Supports Linux, macOS and Windows with installed .NET Framework 4.6.1+. This version is deprecated"
        else -> null
    }

    private val postfix get(): String = when {
        usingDotNetFramework -> CoverageConstants.DOTCOVER_WINDOWS_ONLY_POSTFIX
        usingAgentRuntime -> CoverageConstants.DOTCOVER_CROSS_PLATFORM_POSTFIX
        usingBundledRuntime -> CoverageConstants.DOTCOVER_CROSS_PLATFORM_POSTFIX
        else -> CoverageConstants.DOTCOVER_POSTFIX
    }

    private val usingDotNetFramework get() =
        _packageId == CoverageConstants.DOTCOVER_PACKAGE_ID && super.getVersion() < "2023.3.3-tc01"

    private val usingAgentRuntime get() =
        _packageId == CoverageConstants.DOTCOVER_PACKAGE_ID && super.getVersion() >= "2023.3.3-tc01"

    private val usingBundledRuntime get() =
        _packageId == CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID
}
