/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        agent.configuration.addConfigurationParameter("$ToolPrefix.${DotnetConstants.INTEGRATION_PACKAGE_TYPE}.DEFAULT", default.absolutePath)
        agent.configuration.addConfigurationParameter("$ToolPrefix.${DotnetConstants.INTEGRATION_PACKAGE_TYPE}.BUNDLED", default.absolutePath)
        agent.configuration.addConfigurationParameter("$ToolPrefix.${default.name}", default.absolutePath)
    }

    companion object {
        private val LOG = Logger.getLogger(ToolsPropertiesExtension::class.java)
        private const val ToolPrefix = "teamcity.tool"
    }
}