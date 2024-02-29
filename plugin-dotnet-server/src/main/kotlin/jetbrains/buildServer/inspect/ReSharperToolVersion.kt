package jetbrains.buildServer.inspect

import jetbrains.buildServer.inspect.CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
import jetbrains.buildServer.tools.SimpleToolVersion
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersionIdHelper.getToolId

class ReSharperToolVersion(
    toolType: ToolType,
    version: String
) : SimpleToolVersion(
    toolType,
    version,
    if (isBundled(version)) BUNDLED_TOOL_ID else getToolId(toolType, version),
    getToolId(toolType, version),
    toolType.displayName + " " + version,
    isBundled(version),
    MIN_REQUIRED_FREE_DISK_SPACE_HINT
) {

    companion object {
        const val BUNDLED_VERSION = "2023.1.1"
        private val BUNDLED_TOOL_ID = getToolId(JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID, "bundled")
        private const val MIN_REQUIRED_FREE_DISK_SPACE_HINT = 400L * 1024L * 1024L // 400 MB

        private fun isBundled(version: String) = version == BUNDLED_VERSION
    }
}