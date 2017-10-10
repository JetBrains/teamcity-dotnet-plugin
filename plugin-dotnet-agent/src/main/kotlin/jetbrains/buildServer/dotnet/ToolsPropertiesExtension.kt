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
            LOG.info("\"$toolsPath\" was not found")
            return
        }

        val packages = _fileSystemService.list(toolsPath).toList()
        if (packages.isEmpty()) {
            LOG.info("\"$toolsPath\" has no any packages")
            return
        }

        for (integrationPackage in packages) {
            agent.configuration.addConfigurationParameter("$ToolPrefix.${integrationPackage.name}", integrationPackage.absolutePath)
            LOG.info("Found .NET integration package at \"${integrationPackage.absolutePath}\"")
        }

        val default = packages.first()
        agent.configuration.addConfigurationParameter("$ToolPrefix.${DotnetConstants.PACKAGE_TYPE}.DEFAULT", default.absolutePath)
        agent.configuration.addConfigurationParameter("$ToolPrefix.${DotnetConstants.PACKAGE_TYPE}.BUNDLED", default.absolutePath)
    }

    companion object {
        private val LOG = Logger.getInstance(ToolsPropertiesExtension::class.java.name)

        private const val ToolPrefix = "teamcity.tool"
    }
}