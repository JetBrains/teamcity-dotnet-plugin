package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.util.FileUtil
import java.io.File

/**
 * Provides download specification, for example:
 * <Download Id="Plugin.Id" Version="1.2.0.0"></Download>
 */
class DownloadPluginSource
    : PluginSource {
    override val id = "download"

    override fun getPlugin(specification: String) =
            specification.split("/").let {
                parts ->
                val result = E("Download")
                if (parts.size == 2) {
                    result
                            .a("Id", parts[0])
                            .a("Version", parts[1])
                }

                result
        }
}