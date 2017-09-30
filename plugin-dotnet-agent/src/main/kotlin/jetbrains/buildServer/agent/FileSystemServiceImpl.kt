package jetbrains.buildServer.agent

import org.apache.commons.io.FileUtils
import java.io.*

class FileSystemServiceImpl : FileSystemService {
    override fun write(file: File, writer: (OutputStream) -> Unit) = FileOutputStream(file).use(writer)

    override fun read(file: File, reader: (InputStream) -> Unit) = FileInputStream(file).use(reader)

    override fun isExists(file: File): Boolean = file.exists()

    override fun copy(source: File, destination: File): Unit = FileUtils.copyDirectory(source, destination)

    override fun remove(file: File): Unit = FileUtils.deleteDirectory(file)
}