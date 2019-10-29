package jetbrains.buildServer.agent

import java.io.File

data class ToolPath(val path: Path, val virtualPath: Path = path, val homePaths: List<Path> = emptyList<Path>())