package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.io.File

class ImportDataServiceMessage(
        coverageToolName: String,
        artifactPath: Path)
    : ServiceMessage(
        "importData",
        mapOf(
                "type" to "dotNetCoverage",
                "tool" to coverageToolName,
                "path" to artifactPath.path)) {
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return toString() == other.toString()
    }
}