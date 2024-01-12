

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import java.io.File

/**
 * Provides file specification, for example:
 * <File Path="C:\temp\file.nupkg"></File>
 */
class FilePluginXmlElementGenerator(
    private val _fileSystemService: FileSystemService,
    private val _loggerService: LoggerService
) : PluginXmlElementGenerator {
    override val sourceId = "file"

    override fun generateXmlElement(strValue: String) =
        File(strValue).let { file ->
            val result = XmlElement("File")
            if (_fileSystemService.isExists(file) && _fileSystemService.isFile(file)) {
                result.withAttribute("Path", file.canonicalFile.absolutePath)
            } else {
                _loggerService.writeWarning("Invalid R# CLT plugin file descriptor, file $file does not exist or is not a file, it will be ignored.")
            }

            result
        }
}