package jetbrains.buildServer.runners

import java.io.File

class FileSystemServiceImpl : FileSystemService {
    override fun isExists(file: File): Boolean {
        return file.exists()
    }
}