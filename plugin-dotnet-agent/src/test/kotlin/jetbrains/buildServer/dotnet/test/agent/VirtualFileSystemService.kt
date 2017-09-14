package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.FileSystemService
import java.io.*
import java.io.PipedInputStream
import java.io.PipedOutputStream

class VirtualFileSystemService : FileSystemService {
    private val _directories: HashSet<File> = HashSet<File>()
    private val _files: MutableMap<File, FileInfo> = mutableMapOf()

    override fun write(file: File, writer: (OutputStream) -> Unit) {
        addFile(file)
        writer(_files[file]!!.outputStream)
    }

    override fun read(file: File, reader: (InputStream) -> Unit) {
        reader(_files.get(file)!!.inputStream)
    }

    fun addDirectory(directory: File): VirtualFileSystemService
    {
        _directories.add(directory);
        var parent: File? = directory;
        while (parent != null){
            parent = parent.parentFile;
            if(parent != null) {
                if(!_directories.contains(parent)) {
                    _directories.add(parent);
                }
            }
        }

        return this
    }

    fun addFile(file: File): VirtualFileSystemService
    {
        val parent = file.parentFile;
        if (parent != null) {
            addDirectory(parent);
        }

        if (!_files.containsKey(file)) {
            _files.put(file, FileInfo());
        }

        return this
    }

    override fun isExists(file: File): Boolean = _directories.contains(file) || _files.contains(file)

    class FileInfo {
        public val inputStream: InputStream
        public val outputStream: OutputStream
        init {
            outputStream = PipedOutputStream()
            inputStream = PipedInputStream(outputStream)
        }
    }
}