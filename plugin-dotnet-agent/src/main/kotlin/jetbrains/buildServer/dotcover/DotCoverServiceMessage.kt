

package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class DotCoverServiceMessage(
        dotCoverPath: Path)
    : ServiceMessage(
        "dotNetCoverageDotnetRunner",
        mapOf("dotcover_home" to dotCoverPath.path)) {
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return toString() == other.toString()
    }
}