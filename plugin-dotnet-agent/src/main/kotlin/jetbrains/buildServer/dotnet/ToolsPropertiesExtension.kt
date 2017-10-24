package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

/**
 * Provides a list of available integration package tools.
 */
class ToolsPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _pluginDescriptor: PluginDescriptor,
        private val _fileSystemService: FileSystemService)
    : AgentLifeCycleAdapter() {
    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.debug("Locating .NET integration packages")

        val toolsPath = File(_pluginDescriptor.pluginRoot, "tools")
        if (!_fileSystemService.isExists(toolsPath)) {
            LOG.warn("\"$toolsPath\" was not found")
            return
        }

        val packages = _fileSystemService.list(toolsPath).toList()
        if (packages.isEmpty()) {
            LOG.warn("\"$toolsPath\" has no any packages")
            return
        }

        for (integrationPackage in packages) {
            LOG.debug("Found .NET integration package at \"${integrationPackage.absolutePath}\"")
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ToolsPropertiesExtension::class.java.name)
    }
}