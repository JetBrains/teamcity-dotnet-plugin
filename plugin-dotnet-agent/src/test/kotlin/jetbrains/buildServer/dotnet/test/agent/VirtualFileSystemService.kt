package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.FileSystemService
import java.io.*

class VirtualFileSystemService : FileSystemService {
    private val _directories: MutableMap<File, DirectoryInfo> = mutableMapOf()
    private val _files: MutableMap<File, FileInfo> = mutableMapOf()

    override fun write(file: File, writer: (OutputStream) -> Unit) {
        addFile(file)
        writer(_files[file]!!.outputStream)
    }

    override fun read(file: File, reader: (InputStream) -> Unit) {
        reader(_files[file]!!.inputStream)
    }

    fun addDirectory(directory: File, attributes: Attributes = Attributes()): VirtualFileSystemService {
        _directories[directory] = DirectoryInfo(attributes)
        var parent: File? = directory
        while (parent != null) {
            parent = parent.parentFile
            if (parent != null) {
                if (!_directories.contains(parent)) {
                    _directories[parent] = DirectoryInfo(attributes)
                }
            }
        }

        return this
    }

    fun addFile(file: File, attributes: Attributes = Attributes()): VirtualFileSystemService {
        val parent = file.parentFile
        if (parent != null) {
            addDirectory(parent)
        }

        if (!_files.containsKey(file)) {
            _files[file] = FileInfo(attributes)
        }

        return this
    }

    override fun isExists(file: File): Boolean = _directories.contains(file) || _files.contains(file)

    override fun isDirectory(file: File): Boolean = _directories.contains(file)

    override fun isAbsolute(file: File): Boolean = _directories[file]?.attributes?.isAbsolute ?: _files[file]?.attributes?.isAbsolute ?: false

    override fun copy(source: File, destination: File) {
        if (!isDirectory(source)) {
            val sourceFile = _files[source]!!
            addFile(destination, sourceFile.attributes)
            _files[destination] = sourceFile
        } else {
            val sourceDir = _directories[source]!!
            addDirectory(destination, sourceDir.attributes)
            _directories[destination] = sourceDir
        }
    }

    override fun remove(file: File) {
        val fileInfo = _files[file]
        if (fileInfo != null) {
            val errorOnRemove = fileInfo.attributes.errorOnRemove
            if (errorOnRemove != null) {
                throw errorOnRemove
            }

            _files.remove(file)
        }

        val dirInfo = _directories[file]
        if (dirInfo != null) {
            val errorOnRemove = dirInfo.attributes.errorOnRemove
            if (errorOnRemove != null) {
                throw errorOnRemove
            }

            _directories.remove(file)
        }
    }

    override fun list(file: File): Sequence<File> = _directories.keys.asSequence().plus(_files.map { it.key }).filter { it.parentFile == file }

    private data class FileInfo(val attributes: Attributes) {
        val inputStream: InputStream
        val outputStream: OutputStream

        init {
            outputStream = PipedOutputStream()
            inputStream = PipedInputStream(outputStream)
        }
    }

    private data class DirectoryInfo(val attributes: Attributes)

    class Attributes {
        var isAbsolute: Boolean = false
        var errorOnRemove: Exception? = null
    }

    companion object {
        fun absolute(isAbsolute: Boolean = true): Attributes {
            val attr = Attributes()
            attr.isAbsolute = isAbsolute
            return attr
        }

        fun errorOnRemove(errorOnRemove: Exception): Attributes {
            val attr = Attributes()
            attr.errorOnRemove = errorOnRemove
            return attr
        }
    }
}