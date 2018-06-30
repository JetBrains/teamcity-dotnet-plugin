package jetbrains.buildServer.dotnet

import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class FileSystemServiceImpl : FileSystemService {
    override fun write(file: File, writer: (OutputStream) -> Unit) {
        FileUtil.createParentDirs(file)
        FileOutputStream(file).use(writer)
    }

    override fun list(file: File): Sequence<File> = file.listFiles()?.asSequence() ?: emptySequence()

    override fun isExists(file: File): Boolean = file.exists()
}