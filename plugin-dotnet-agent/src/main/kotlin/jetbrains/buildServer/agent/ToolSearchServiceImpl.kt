package jetbrains.buildServer.agent

import java.io.File

class ToolSearchServiceImpl(private val _environment: Environment,
                            private val _fileSystem: FileSystemService)
    : ToolSearchService {
    override fun find(toolName: String,
                      environmentVariableName: String,
                      additionalPaths: Sequence<File>,
                      basePathResolver: (File) -> File): Sequence<File> {
        val pathFromEnvironment = _environment.tryGetVariable(environmentVariableName)?.let {
            sequenceOf(File(it))
        } ?: emptySequence()

        val pattern = Regex("^$toolName(\\.(exe))?$")
        return pathFromEnvironment.plus(additionalPaths).plus(_environment.paths)
                .flatMap { _fileSystem.list(basePathResolver(it)) }
                .filter { it.name.matches(pattern) }
    }
}