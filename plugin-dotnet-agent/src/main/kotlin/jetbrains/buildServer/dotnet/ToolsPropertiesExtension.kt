package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

/**
 * Provides a list of available integration package tools.
 */
class ToolsPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private final val _pluginDescriptor: PluginDescriptor,
        private val _fileSystemService: FileSystemService)
    : AgentLifeCycleAdapter() {
    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating .NET integration packages")

        val toolsPath = File(_pluginDescriptor.pluginRoot, "tools")
        if( !_fileSystemService.isExists(toolsPath)) {
            LOG.info("\"$toolsPath\" was not found")
            return
        }
        for (integrationPackage in _fileSystemService.list(toolsPath)) {
            agent.configuration.addConfigurationParameter("teamcity.tool.${integrationPackage.name}", integrationPackage.absolutePath)
            LOG.info("Found .NET integration package at \"${integrationPackage.absolutePath}\"")
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ToolsPropertiesExtension::class.java.name)
    }
}