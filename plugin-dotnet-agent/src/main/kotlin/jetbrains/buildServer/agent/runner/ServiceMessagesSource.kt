package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import java.io.File

class ServiceMessagesSource(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _indicesSource: Source<Index>)
    : Source<String> {
    override fun read(source: String, fromPosition: Long, count: Long) = sequence {
        if (source.isNullOrBlank() || count < 0L) {
            throw IllegalArgumentException()
        }

        val sourceFile = File(_pathsService.getPath(PathType.AgentTemp), source + ".msg")
        if (_fileSystemService.isExists(sourceFile) && _fileSystemService.isFile(sourceFile)) {
            for (index in _indicesSource.read(source, fromPosition, count)) {
                val bytes = ByteArray(index.size.toInt())
                if (_fileSystemService.readBytes(sourceFile, index.fromPosition, bytes) == bytes.size) {
                    yield(String(bytes, Charsets.UTF_8).trimEnd())
                } else {
                    LOG.warn("Cannot read \"$sourceFile\".")
                }
            }

        } else {
            LOG.debug("Cannot find \"$sourceFile\".")
        }
    }

    companion object {
        private val LOG = Logger.getLogger(ServiceMessagesSource::class.java)
    }
}