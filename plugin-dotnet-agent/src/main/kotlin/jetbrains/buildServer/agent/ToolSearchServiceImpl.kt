package jetbrains.buildServer.agent

import java.io.File

class ToolSearchServiceImpl(
        private val _fileSystem: FileSystemService): ToolSearchService {

    override fun find(toolName: String, paths: Sequence<Path>): Sequence<File> {
        val pattern = Regex("^$toolName(\\.(exe))?$")
        return paths
                .flatMap { _fileSystem.list(File(it.path)) }
                .filter { it.name.matches(pattern) }
                .distinct()
    }
}