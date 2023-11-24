package jetbrains.buildServer.dotCover

import jetbrains.buildServer.tools.ToolVersion
import jetbrains.buildServer.tools.utils.SemanticVersion

class DotCoverToolComparator : Comparator<ToolVersion> {

    override fun compare(toolVersion1: ToolVersion, toolVersion2: ToolVersion): Int {
        if (toolVersion1 !is DotCoverToolVersion || toolVersion2 !is DotCoverToolVersion) {
            return 0
        }

        val result = SemanticVersion.compareAsVersions(toolVersion1.packageVersion, toolVersion2.packageVersion)

        return if (result != 0) {
            result
        } else toolVersion1.packageId.compareTo(toolVersion2.packageId)
    }
}
