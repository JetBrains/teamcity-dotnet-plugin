

package jetbrains.buildServer.agent.runner

import java.io.File
import java.nio.file.Path

interface PathsService {
    val uniqueName: String

    fun uniqueName(basePath: File, extension: String): File

    fun getPath(pathType: PathType): File

    fun getPath(pathType: PathType, runnerType: String): File

    fun getTempFileName(extension: String): File

    fun resolvePath(pathType: PathType, relativePath: String): Path
}