

package jetbrains.buildServer

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface FileSystemService {
    fun read(file: File, reader: (InputStream) -> Unit)

    fun write(file: File, writer: (OutputStream) -> Unit)

    fun list(file: File): Sequence<File>

    fun isExists(file: File): Boolean

    fun isFile(file: File): Boolean

    fun copy(fileFrom: File, fileTo: File)
}