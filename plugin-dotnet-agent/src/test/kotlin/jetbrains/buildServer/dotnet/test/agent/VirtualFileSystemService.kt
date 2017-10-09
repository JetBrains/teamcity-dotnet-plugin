package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.FileSystemService
import java.io.*
import java.io.PipedInputStream
import java.io.PipedOutputStream

class VirtualFileSystemService : FileSystemService {
    private val _directories: HashSet<File> = hashSetOf()
    private val _files: MutableMap<File, FileInfo> = mutableMapOf()

    override fun write(file: File, writer: (OutputStream) -> Unit) {
        addFile(file)
        writer(_files[file]!!.outputStream)
    }

    override fun read(file: File, reader: (InputStream) -> Unit) {
        reader(_files[file]!!.inputStream)
    }

    fun addDirectory(directory: File): VirtualFileSystemService
    {
        _directories.add(directory)
        var parent: File? = directory
        while (parent != null){
            parent = parent.parentFile
            if(parent != null) {
                if(!_directories.contains(parent)) {
                    _directories.add(parent)
                }
            }
        }

        return this
    }

    fun addFile(file: File): VirtualFileSystemService
    {
        val parent = file.parentFile
        if (parent != null) {
            addDirectory(parent)
        }

        if (!_files.containsKey(file)) {
            _files.put(file, FileInfo())
        }

        return this
    }

    override fun isExists(file: File): Boolean = _directories.contains(file) || _files.contains(file)

    override fun copy(source: File, destination: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(file: File) {
        if(_files.containsKey(file)) {
            _files.remove(file)
        }

        _directories.remove(file)
    }

    override fun list(file: File): Sequence<File> = _directories.asSequence().plus(_files.map { it.key }).filter { it.parentFile == file }

    class FileInfo {
        val inputStream: InputStream
        val outputStream: OutputStream
        init {
            outputStream = PipedOutputStream()
            inputStream = PipedInputStream(outputStream)
        }
    }
}