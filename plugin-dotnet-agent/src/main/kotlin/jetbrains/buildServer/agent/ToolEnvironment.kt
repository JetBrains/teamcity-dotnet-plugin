package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.Path

interface ToolEnvironment {
    val homePaths: Sequence<Path>

    val defaultPaths: Sequence<Path>

    val environmentPaths: Sequence<Path>
}