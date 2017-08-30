package jetbrains.buildServer.runners

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface FileSystemService {
    fun isExists(file: File): Boolean

    fun write(file: File, writer: (OutputStream) -> Unit)

    fun read(file: File, reader: (InputStream) -> Unit)
}