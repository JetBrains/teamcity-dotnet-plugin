

package jetbrains.buildServer

import jetbrains.buildServer.util.FileUtil
import java.io.*

class FileSystemServiceImpl : FileSystemService {
    override fun read(file: File, reader: (InputStream) -> Unit) = FileInputStream(file).use(reader)

    override fun write(file: File, writer: (OutputStream) -> Unit) {
        FileUtil.createParentDirs(file)
        FileOutputStream(file).use(writer)
    }

    override fun list(file: File): Sequence<File> = file.listFiles()?.asSequence() ?: emptySequence()

    override fun isExists(file: File): Boolean = file.exists()

    override fun isFile(file: File): Boolean = file.isFile

    override fun copy(fileFrom: File, fileTo: File) = FileUtil.copy(fileFrom, fileTo)
}