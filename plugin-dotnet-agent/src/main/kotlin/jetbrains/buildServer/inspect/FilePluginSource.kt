package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.util.FileUtil
import java.io.File

/**
 * Provides file specification, for example:
 * <File Path="C:\temp\file.nupkg"></File>
 */
class FilePluginSource(
        private val _fileSystemService: FileSystemService)
    : PluginSource {
    override val id = "file"

    override fun getPlugin(specification: String) =
        File(specification).let {
            file ->
            val result = E("File")
            if (_fileSystemService.isExists(file) && _fileSystemService.isFile(file)) {
                result.a("Path", file.canonicalFile.absolutePath)
            }

            result
        }
}