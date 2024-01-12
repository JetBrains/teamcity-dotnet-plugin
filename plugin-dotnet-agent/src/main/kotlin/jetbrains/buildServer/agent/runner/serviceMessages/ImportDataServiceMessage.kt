

package jetbrains.buildServer.agent.runner.serviceMessages

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class ImportDataServiceMessage(
        dataProcessorType: String,
        artifactPath: Path,
        tool: String)
    : ServiceMessage(
        "importData",
        mapOf(
                "type" to dataProcessorType,
                "tool" to tool,
                "path" to artifactPath.path)
                .filter { it.value.isNotEmpty() }) {
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return toString() == other.toString()
    }
}