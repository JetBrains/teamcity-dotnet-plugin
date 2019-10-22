package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Path

class PathResolverState(public val pathToResolve: Path) {
    public var resolvedPath: Path? = null
}