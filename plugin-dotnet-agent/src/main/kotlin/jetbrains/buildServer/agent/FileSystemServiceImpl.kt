package jetbrains.buildServer.agent

import org.apache.commons.io.FileUtils
import java.io.*

class FileSystemServiceImpl : FileSystemService {
    override fun isExists(file: File): Boolean = file.exists()

    override fun isDirectory(file: File): Boolean = file.isDirectory

    override fun isAbsolute(file: File): Boolean = file.isAbsolute

    override fun write(file: File, writer: (OutputStream) -> Unit) = FileOutputStream(file).use(writer)

    override fun read(file: File, reader: (InputStream) -> Unit) = FileInputStream(file).use(reader)

    override fun copy(source: File, destination: File) = FileUtils.copyDirectory(source, destination)

    override fun remove(file: File) = FileUtils.deleteDirectory(file)

    override fun list(file: File): Sequence<File> = file.listFiles()?.asSequence() ?: emptySequence()
}