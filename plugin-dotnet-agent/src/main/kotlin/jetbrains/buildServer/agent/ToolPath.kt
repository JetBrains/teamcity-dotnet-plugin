

package jetbrains.buildServer.agent

data class ToolPath(
    val path: Path,
    val virtualPath: Path = path,
    val homePaths: List<Path> = emptyList<Path>(),
)