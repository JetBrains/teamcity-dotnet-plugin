package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.runners.FileSystemService
import java.io.File

class VirtualFileSystemService : FileSystemService {
    private val _directories: HashSet<File> = HashSet<File>()
    private val _files: HashSet<File> = HashSet<File>()

    fun addDirectory(directory: File): VirtualFileSystemService
    {
        _directories.add(directory);
        var parent: File? = directory;
        while (parent != null){
            parent = parent.parentFile;
            if(parent != null) {
                _directories.add(parent);
            }
        }

        return this
    }

    fun addFile(file: File): VirtualFileSystemService
    {
        val parent = file.parentFile;
        if(parent != null) {
            addDirectory(parent);
        }

        _files.add(file);
        return this
    }

    override fun isExists(file: File): Boolean = _directories.contains(file) || _files.contains(file)
}