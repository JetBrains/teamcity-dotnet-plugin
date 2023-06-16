package jetbrains.buildServer.dotnet.coverage.utils

import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.IOException

class FileServiceImpl : FileService {

    override fun exists(path: File): Boolean {
        return path.exists()
    }

    override fun isDirectory(path: File): Boolean {
        return path.isDirectory
    }

    @Throws(IOException::class)
    override fun createFile(file: File): Boolean {
        return file.createNewFile()
    }

    override fun createDirectory(path: File): Boolean {
        return path.mkdirs()
    }

    override fun sanitizeFileName(name: String): String {
        return FileUtil.sanitizeFileName(name)
    }

    @Throws(IOException::class)
    override fun copyFile(from: File, to: File) {
        FileUtil.copy(from, to)
    }
}
