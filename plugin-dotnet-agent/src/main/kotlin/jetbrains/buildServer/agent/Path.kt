package jetbrains.buildServer.agent

import java.io.File

data class Path(val path: File, val virtualPath: File = path)