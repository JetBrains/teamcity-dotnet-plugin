

package jetbrains.buildServer.agent

import jetbrains.buildServer.util.OSType
import java.io.File

class ToolSearchServiceImpl(
        private val _fileSystem: FileSystemService,
        private val _environment: Environment): ToolSearchService {

    override fun find(toolName: String, paths: Sequence<Path>): Sequence<File> {
        val executableName = when(_environment.os) {
            OSType.UNIX, OSType.MAC -> toolName
            OSType.WINDOWS -> "$toolName.exe"
        }

        val pattern = Regex("^$executableName$")
        return paths
                .flatMap { _fileSystem.list(File(it.path)) }
                .filter { it.name.matches(pattern) }
                .distinct()
    }
}