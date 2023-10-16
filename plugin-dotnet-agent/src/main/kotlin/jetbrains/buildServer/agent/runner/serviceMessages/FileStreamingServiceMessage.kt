package jetbrains.buildServer.agent.runner.serviceMessages

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.nio.charset.Charset

class FileStreamingServiceMessage(
    filePath: String?,
    filePattern: String?,
    charset: Charset? = null,
    wrapFileContentInBlock: Boolean? = null,
    quietMode: Boolean? = null
) : ServiceMessage(
    "importData",
    mapOf(
        "type" to "streamToBuildLog",
        "filePath" to filePath,
        "filePattern" to filePattern,
        "charset" to charset?.name(),
        "wrapFileContentInBlock" to wrapFileContentInBlock?.toString(),
        "quiet" to quietMode?.toString()
    ).filter { it.value != null })
