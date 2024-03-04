package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.PathType

interface PathResolver {
    fun resolve(paths: Sequence<String>, basePathType: PathType = PathType.WorkingDirectory): Sequence<Path>
}