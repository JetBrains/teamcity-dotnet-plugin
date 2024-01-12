

package jetbrains.buildServer.agent

interface ToolEnvironment {
    val homePaths: Sequence<Path>

    val defaultPaths: Sequence<Path>

    val environmentPaths: Sequence<Path>
}