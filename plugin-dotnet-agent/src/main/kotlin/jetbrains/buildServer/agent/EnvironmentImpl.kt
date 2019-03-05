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
            (tryGetVariable(PathEnvironmentVariableName)?.let { path ->
                StringUtil.splitHonorQuotes(path, File.pathSeparatorChar)
                        .asSequence()
                        .map { File(it) }
                        .filter { _fileSystemService.isExists(it) }
            } ?: emptySequence()) + getHintPaths()

    override val os: OSType
        get() = OSDetector.detect() ?: OSType.UNIX

    /**
     * Provides a well known paths for tools on each platform.
     */
    private fun getHintPaths(): Sequence<File> = sequence {
        when (os) {
            OSType.MAC -> yield(File("/usr/local/share/dotnet"))
            OSType.UNIX -> yield(File("/usr/share/dotnet"))
            OSType.WINDOWS -> yield(File("C:\\Program Files\\dotnet"))
        }
    }

    companion object {
        private const val PathEnvironmentVariableName = "PATH"
        private val OSDetector = OSTypeDetector()
    }
}