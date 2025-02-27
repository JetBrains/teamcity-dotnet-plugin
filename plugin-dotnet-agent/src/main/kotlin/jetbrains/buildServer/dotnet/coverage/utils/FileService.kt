package jetbrains.buildServer.dotnet.coverage.utils

import java.io.File
import java.io.IOException

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface FileService {

    fun isDirectory(path: File): Boolean

    fun exists(path: File): Boolean

    @Throws(IOException::class)
    fun createFile(file: File): Boolean

    fun createDirectory(path: File): Boolean

    fun sanitizeFileName(name: String): String

    @Throws(IOException::class)
    fun copyFile(from: File, to: File)
}
