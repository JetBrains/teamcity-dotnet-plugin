package jetbrains.buildServer.dotcover

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.io.File

class ImportDataServiceMessage(
        coverageToolName: String,
        artifactPath: File)
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
        return toString().equals(other.toString())
    }
}