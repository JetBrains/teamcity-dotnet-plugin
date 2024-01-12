
package jetbrains.buildServer.agent

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface FileSystemService {
    fun createFile(path: String): File

    fun createFile(parent: File, child: String): File

    fun getExistingFile(path: String): Result<File>

    fun isExists(file: File): Boolean

    fun isDirectory(file: File): Boolean

    fun isFile(file: File): Boolean

    fun getLength(file: File): Long

    fun isAbsolute(file: File): Boolean

    fun <T>write(file: File, writer: (OutputStream) -> T): T

    fun <T>read(file: File, reader: (InputStream) -> T): T

    fun readBytes(file: File, operations: Sequence<FileReadOperation>): Sequence<FileReadOperationResult>

    fun copy(sourceDirectory: File, destinationDirectory: File)

    fun remove(fileOrDirectory: File): Boolean

    fun list(directory: File): Sequence<File>

    fun createDirectory(directory: File): Boolean

    fun sanitizeFileName(name: String): String

    fun generateTempFile(path: File, prefix: String, extension: String): File
}