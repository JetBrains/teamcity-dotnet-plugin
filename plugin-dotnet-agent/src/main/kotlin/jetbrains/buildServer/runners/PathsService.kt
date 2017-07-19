package jetbrains.buildServer.runners

import java.io.File

interface PathsService {
    fun getPath(pathType : PathType): File

    fun getToolPath(toolName: String): File
}