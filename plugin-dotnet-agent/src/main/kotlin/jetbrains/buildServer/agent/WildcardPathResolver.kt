package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class WildcardPathResolver(
    private val _pathsService: PathsService,
    private val _pathMatcher: PathMatcher,
    private val _fileSystemService: FileSystemService,
    private val _virtualContext: VirtualContext,
) : PathResolver {
    override fun resolve(paths: Sequence<String>, basePathType: PathType): Sequence<Path> {
        val basePath = _pathsService.getPath(basePathType)
        return paths
            .partition { wildcardPattern.matches(it) }
            .let { (withWildcards, withoutWildcards) -> sequence<File> {
                yieldAll(withoutWildcards.map { File(it) })
                yieldAll(withWildcards.flatMap { _pathMatcher.match(basePath, listOf(it)) })
            }}
            .partition { _fileSystemService.isAbsolute(it) }
            .let { (absolutePaths, relativePaths) -> sequence<File> {
                yieldAll(relativePaths.map { _fileSystemService.createFile(basePath, it.path) })
                yieldAll(absolutePaths)
            }}
            .map { Path(_virtualContext.resolvePath(it.path)) }
    }

    companion object {
        internal val wildcardPattern = Regex(".*[?*].*")
    }
}