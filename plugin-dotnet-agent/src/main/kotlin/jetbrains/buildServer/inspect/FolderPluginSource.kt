package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.util.FileUtil
import java.io.File

/**
 * Provides folder specification, for example:
 * <Folder Path="C:\temp"></Folder>
 */
class FolderPluginSource(
        private val _fileSystemService: FileSystemService)
    : PluginSource {
    override val id = "folder"

    override fun getPlugin(specification: String) =
        File(specification).let {
            directory ->
            val result = E("Folder")
            if (_fileSystemService.isExists(directory) && _fileSystemService.isDirectory(directory)) {
                result.a("Path", directory.canonicalFile.absolutePath)
            }

            result
        }
}