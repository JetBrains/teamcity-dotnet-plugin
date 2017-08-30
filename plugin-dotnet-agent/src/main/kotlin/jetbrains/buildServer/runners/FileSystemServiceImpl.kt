package jetbrains.buildServer.runners

import java.io.*

class FileSystemServiceImpl : FileSystemService {
    override fun write(file: File, writer: (OutputStream) -> Unit) {
        FileOutputStream(file).use(writer)
    }

    override fun read(file: File, reader: (InputStream) -> Unit) {
        FileInputStream(file).use(reader)
    }

    override fun isExists(file: File): Boolean {
        return file.exists()
    }
}