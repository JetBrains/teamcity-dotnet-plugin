package jetbrains.buildServer.agent.runner

import java.io.File

interface PathsService {
    val uniqueName: String

    fun uniqueName(basePath: File, extension: String): File

    fun getPath(pathType: PathType): File

    fun getTempFileName(extension: String): File
}