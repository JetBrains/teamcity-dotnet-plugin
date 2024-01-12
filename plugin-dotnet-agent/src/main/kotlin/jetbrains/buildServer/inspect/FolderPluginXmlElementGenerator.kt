

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import java.io.File

/**
 * Provides folder specification, for example:
 * <Folder Path="C:\temp"></Folder>
 */
class FolderPluginXmlElementGenerator(
    private val _fileSystemService: FileSystemService,
    private val _loggerService: LoggerService
) : PluginXmlElementGenerator {
    override val sourceId = "folder"

    override fun generateXmlElement(strValue: String) =
        File(strValue).let { directory ->
            val result = XmlElement("Folder")
            if (_fileSystemService.isExists(directory) && _fileSystemService.isDirectory(directory)) {
                result.withAttribute("Path", directory.canonicalFile.absolutePath)
            } else {
                _loggerService.writeWarning("Invalid R# CLT plugin folder descriptor, folder $directory does not exist or is not a directory, it will be ignored.")
            }

            result
        }
}