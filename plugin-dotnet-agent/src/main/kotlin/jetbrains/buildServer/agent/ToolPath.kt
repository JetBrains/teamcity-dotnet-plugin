package jetbrains.buildServer.agent

import java.io.File

data class ToolPath(val path: File, val virtualPath: File = path)