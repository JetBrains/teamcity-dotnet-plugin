

package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.impl.OSTypeDetector
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.StringUtil
import java.io.File

class EnvironmentImpl(
        private val _fileSystemService: FileSystemService,
        private val _osTypeDetector: OSTypeDetector)
    : Environment {

    override fun tryGetVariable(name: String): String? {
        return System.getenv(name)
    }

    override val paths: Sequence<Path> get() =
            tryGetVariable(PathEnvironmentVariableName)?.let { path ->
                StringUtil.splitHonorQuotes(path, File.pathSeparatorChar)
                        .asSequence()
                        .map { Path(it) }
                        .filter { _fileSystemService.isExists(File(it.path)) }
            } ?: emptySequence()

    override val os: OSType
        get() = _osTypeDetector.detect() ?: OSType.UNIX

    override val osName: String?
        get() = System.getProperty("os.name")

    companion object {
        private const val PathEnvironmentVariableName = "PATH"
    }
}