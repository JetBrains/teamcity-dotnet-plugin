
package jetbrains.buildServer.agent

import jetbrains.buildServer.util.FileUtil
import org.apache.commons.io.FileUtils
import java.io.*

class FileSystemServiceImpl: FileSystemService {
    override fun createFile(path: String) = File(path)

    override fun createFile(parent: File, child: String) = File(parent, child)

    override fun getExistingFile(path: String): Result<File> =
        File(path)
            .let { when {
                !isExists(it) || !isFile(it) -> {
                    Result.failure(Error("File \"$it\" doesn't exist or it is not a file"))
                }
                else -> Result.success(it)
            } }

    override fun isExists(file: File): Boolean = file.exists()

    override fun isDirectory(file: File): Boolean = file.isDirectory

    override fun isFile(file: File): Boolean = file.isFile

    override fun getLength(file: File): Long = file.length()

    override fun isAbsolute(file: File): Boolean = file.isAbsolute

    override fun <T> write(file: File, writer: (OutputStream) -> T) = FileOutputStream(file).use(writer)

    override fun <T> read(file: File, reader: (InputStream) -> T) = FileInputStream(file).use(reader)

    override fun readBytes(file: File, operations: Sequence<FileReadOperation>) = sequence<FileReadOperationResult> {
        RandomAccessFile(file, "r").use {
            for (operation in operations) {
                it.seek(operation.fromPosition)
                yield(FileReadOperationResult(operation, it.read(operation.to)))
            }
        }
    }

    override fun copy(sourceDirectory: File, destinationDirectory: File) = FileUtils.copyDirectory(sourceDirectory, destinationDirectory)

    override fun remove(fileOrDirectory: File) = FileUtil.delete(fileOrDirectory)

    override fun list(directory: File): Sequence<File> = directory.listFiles()?.asSequence() ?: emptySequence()

    override fun createDirectory(directory: File) = directory.mkdirs()

    override fun sanitizeFileName(name: String) = FileUtil.sanitizeFileName(name)

    override fun generateTempFile(path: File, prefix: String, extension: String): File {
        createDirectory(path)
        var sequenceValue = 0L
        do {
            val fileName = sanitizeFileName(prefix + if (sequenceValue == 0L) "" else sequenceValue) + extension
            val file = File(path, fileName)
            if (!isExists(file) && !isDirectory(file)) {
                return file;
            }

            sequenceValue++
        } while (true)
    }
}