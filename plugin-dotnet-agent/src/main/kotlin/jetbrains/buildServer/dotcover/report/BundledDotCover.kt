package jetbrains.buildServer.dotcover.report

import jetbrains.buildServer.agent.BundledToolsRegistry
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.util.FileUtil
import java.io.File

class BundledDotCover(private val _registry: BundledToolsRegistry) {

    val dotCoverPath: File?
        get() {
            val tool = _registry.findTool(CoverageConstants.DOTCOVER_BUNDLED_TOOL_ID) ?: return null
            val path = FileUtil.getCanonicalFile(tool.rootPath)
            return if (!path.isDirectory) null else path
        }
}
