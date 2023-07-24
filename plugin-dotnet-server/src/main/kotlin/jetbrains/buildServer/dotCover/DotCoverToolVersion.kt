package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.tools.SimpleToolVersion
import jetbrains.buildServer.tools.ToolMetadata
import jetbrains.buildServer.tools.ToolMetadataType
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersionIdHelper

open class DotCoverToolVersion (
    toolType: ToolType,
    version: String,
    private val _packageId: String,
    isBundled: Boolean = false
) : SimpleToolVersion(
    toolType,
    version,
    ToolVersionIdHelper.getToolId(
        _packageId,
        if (isBundled) CoverageConstants.BUNDLED_TOOL_VERSION_NAME else version
    )
), ToolMetadata {

    override fun getVersion(): String {
        val postfix = getPostfix(_packageId)
        return if (postfix.length > 0) "${super.getVersion()} $postfix" else super.getVersion()
    }

    val packageVersion: String get() = super.getVersion()

    val packageId: String get() = _packageId

    override fun getDisplayName(): String {
        return "${super.getType().displayName} ${super.getVersion()} ${getPostfix(_packageId)}"
    }

    override fun tryGetMatadata(type: ToolMetadataType): String? {
        return if (isCrossPlatform) "Supports Linux, macOS and Windows with installed .NET Framework 4.6.1+" else null
    }

    fun getPostfix(packageId: String): String {
        return when (packageId) {
            CoverageConstants.DOTCOVER_MACOS_ARM64_PACKAGE_ID -> CoverageConstants.DOTCOVER_MACOS_ARM64_POSTFIX
            CoverageConstants.DOTCOVER_GLOBAL_TOOL_PACKAGE_ID -> CoverageConstants.DOTCOVER_GLOBAL_TOOL_POSTFIX
            CoverageConstants.DOTCOVER_CROSS_PLATFORM_PACKAGE_ID -> CoverageConstants.DOTCOVER_CROSS_PLATFORM_POSTFIX
            else -> CoverageConstants.DOTCOVER_POSTFIX
        }
    }

    private val isCrossPlatform: Boolean
        get() = CoverageConstants.DOTCOVER_CROSS_PLATFORM_PACKAGE_ID == _packageId
}
