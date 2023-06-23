package jetbrains.buildServer.dotCover

import jetbrains.buildServer.tools.ToolVersion
import jetbrains.buildServer.tools.utils.SemanticVersion

class DotCoverToolComparator : Comparator<ToolVersion> {

    override fun compare(tool1: ToolVersion, tool2: ToolVersion): Int {
        if (tool1 !is DotCoverToolVersion || tool2 !is DotCoverToolVersion) {
            return 0
        }

        val toolVersion1: DotCoverToolVersion = tool1
        val toolVersion2: DotCoverToolVersion = tool2

        val result = SemanticVersion.compareAsVersions(toolVersion2.packageVersion, toolVersion1.packageVersion)

        return if (result != 0) {
            result
        } else toolVersion1.packageId.compareTo(toolVersion2.packageId)
    }
}
