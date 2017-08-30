package jetbrains.buildServer.runners

import java.io.File

interface PathsService {
    val uniqueName: String;

    fun getPath(pathType : PathType): File

    fun getToolPath(toolName: String): File
}