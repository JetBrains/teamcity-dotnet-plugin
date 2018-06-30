package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.impl.OSTypeDetector
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.StringUtil
import java.io.File

class EnvironmentImpl(private val _fileSystemService: FileSystemService) : Environment {
    override fun tryGetVariable(name: String): String? {
        return System.getenv(name)
    }

    override val paths: Sequence<File>
        get() =
            tryGetVariable(PathEnvironmentVariableName)?.let {
                return StringUtil.splitHonorQuotes(it, File.pathSeparatorChar)
                        .asSequence()
                        .map { File(it) }
                        .filter { _fileSystemService.isExists(it) }
            } ?: emptySequence()

    override val os: OSType
        get() = OSDetector.detect() ?: OSType.UNIX

    companion object {
        private const val PathEnvironmentVariableName = "PATH"
        private val OSDetector = OSTypeDetector()
    }
}