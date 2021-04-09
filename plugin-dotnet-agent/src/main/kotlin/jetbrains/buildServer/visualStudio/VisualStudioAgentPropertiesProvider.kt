package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_VISUAL_STUDIO
import java.io.File

class VisualStudioAgentPropertiesProvider(
        _visualStudioProviders: List<ToolInstanceProvider>,
        private val _fileSystemService: FileSystemService)
    : AgentPropertiesProvider {

    override val desription = "Visual Studio"

    override val properties =
            _visualStudioProviders
                    .asSequence()
                    .flatMap { it.getInstances().asSequence() }
                    .filter { it.toolType == ToolInstanceType.VisualStudio }
                    .filter {
                        val devenvFile = File(it.installationPath, "devenv.exe")
                        if (!_fileSystemService.isExists(devenvFile) || !_fileSystemService.isFile(devenvFile)) {
                            LOG.debug("Cannot find \"$devenvFile\".")
                            false
                        } else true
                    }
                    .distinctBy { it.baseVersion }
                    .flatMap {
                        visualStudio ->
                        LOG.debug("Found ${visualStudio}.")
                        sequence {
                            yield(AgentProperty(ToolInstanceType.VisualStudio, "$CONFIG_PREFIX_VISUAL_STUDIO${visualStudio.baseVersion}", "${visualStudio.detailedVersion}"))
                            yield(AgentProperty(ToolInstanceType.VisualStudio, "$CONFIG_PREFIX_VISUAL_STUDIO${visualStudio.baseVersion}_Path", visualStudio.installationPath.path))
                        }
                    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioAgentPropertiesProvider::class.java)
    }
}