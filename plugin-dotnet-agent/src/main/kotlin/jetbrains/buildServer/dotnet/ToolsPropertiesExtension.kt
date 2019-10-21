package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildAgent
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor
import jetbrains.buildServer.util.EventDispatcher
import org.apache.log4j.Logger
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

        val default = packages.first()
        agent.configuration.addConfigurationParameter("$ToolPrefix.${DotnetConstants.PACKAGE_TYPE}.DEFAULT", default.absolutePath)
        agent.configuration.addConfigurationParameter("$ToolPrefix.${DotnetConstants.PACKAGE_TYPE}.BUNDLED", default.absolutePath)
        agent.configuration.addConfigurationParameter("$ToolPrefix.${default.name}", default.absolutePath)
    }

    companion object {
        private val LOG = Logger.getLogger(ToolsPropertiesExtension::class.java)
        private const val ToolPrefix = "teamcity.tool"
    }
}