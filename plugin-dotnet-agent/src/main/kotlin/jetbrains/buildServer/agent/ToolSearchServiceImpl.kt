package jetbrains.buildServer.agent

import java.io.File

class ToolSearchServiceImpl(private val _environment: Environment,
                            private val _fileSystem: FileSystemService)
    : ToolSearchService {
    override fun find(toolName: String,
                      environmentVariableName: String,
                      basePathResolver: (File) -> File): Sequence<File> {
        val paths = _environment.tryGetVariable(environmentVariableName)?.let {
            sequenceOf(File(it))
        } ?: emptySequence()

        val pattern = Regex("^$toolName(\\.(exe))?$")
        return paths.plus(_environment.paths)
                .flatMap { _fileSystem.list(basePathResolver(it)) }
                .filter { it.name.matches(pattern) }
    }
}