package jetbrains.buildServer.runners

import java.io.File

interface FileSystemService {
    fun isExists(file: File): Boolean
}