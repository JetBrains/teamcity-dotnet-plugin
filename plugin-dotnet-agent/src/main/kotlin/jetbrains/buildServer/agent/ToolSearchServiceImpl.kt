package jetbrains.buildServer.agent

import java.io.File

class ToolSearchServiceImpl(
        private val _environment: Environment,
        private val _pathMatcher: PathMatcher)
    : ToolSearchService {
    override fun find(
            homePathEnvironmentVariableName: String,
            targets: Sequence<String>,
            basePathResolver: (File) -> File): Sequence<File> {
        val paths = _environment.tryGetVariable(homePathEnvironmentVariableName)?.let {
            sequenceOf(File(it))
        } ?: emptySequence()
        return paths.plus(_environment.paths)
                .map { _pathMatcher.match(basePathResolver(it), targets, emptySequence()) }
                .flatMap { it }
    }
}