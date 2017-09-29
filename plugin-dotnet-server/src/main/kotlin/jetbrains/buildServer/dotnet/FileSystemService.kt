package jetbrains.buildServer.dotnet

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

interface FileSystemService {
    fun write(file: File, writer: (OutputStream) -> Unit)

    fun list(file: File): Sequence<File>

    fun isExists(file: File): Boolean
}