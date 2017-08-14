package jetbrains.buildServer.dotnet

import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class FileSystemServiceImpl : FileSystemService {
    override fun createOutputFile(file: File): OutputStream {
        FileUtil.createParentDirs(file)
        return FileOutputStream(file)
    }
}