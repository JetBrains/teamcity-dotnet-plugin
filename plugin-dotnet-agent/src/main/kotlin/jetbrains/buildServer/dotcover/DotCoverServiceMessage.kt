package jetbrains.buildServer.dotcover

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.io.File

class DotCoverServiceMessage(
        dotCoverPath: File)
    : ServiceMessage(
        "dotNetCoverage",
        mapOf(
                "dotcover_home" to dotCoverPath.path)) {
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return toString().equals(other.toString())
    }
}