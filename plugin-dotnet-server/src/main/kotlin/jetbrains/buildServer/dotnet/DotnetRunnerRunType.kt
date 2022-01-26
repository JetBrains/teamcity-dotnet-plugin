/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.beans.factory.BeanFactory

/**
 * Dotnet runner definition.
 */
class DotnetRunnerRunType(
        private val _factory: BeanFactory,
        private val _pluginDescriptor: PluginDescriptor,
        runTypeRegistry: RunTypeRegistry) : RunType() {

    init {
        _pluginDescriptor.pluginResourcesPath
        runTypeRegistry.registerRunType(this)
    }

    override fun getType(): String {
        return DotnetConstants.RUNNER_TYPE
    }

    override fun getDisplayName(): String {
        return DotnetConstants.RUNNER_DISPLAY_NAME
    }

    override fun getDescription(): String {
        return DotnetConstants.RUNNER_DESCRIPTION
    }

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor {
        return PropertiesProcessor { properties ->
            val command = properties?.get(DotnetConstants.PARAM_COMMAND)
            if (command.isNullOrEmpty()) {
                return@PropertiesProcessor arrayListOf(InvalidProperty(DotnetConstants.PARAM_COMMAND, "Command must be set"))
            }

            val errors = arrayListOf<InvalidProperty>()
            DotnetParametersProvider.commandTypes[command]?.let {
                errors.addAll(it.validateProperties(properties))
            }

            properties[CoverageConstants.PARAM_TYPE]?.let {
                DotnetParametersProvider.coverageTypes[it]?.let {
                    errors.addAll(it.validateProperties(properties))
                }
            }

            errors
        }
    }

    override fun getEditRunnerParamsJspFilePath(): String {
        return _pluginDescriptor.getPluginResourcesPath("editDotnetParameters.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String {
        return _pluginDescriptor.getPluginResourcesPath("viewDotnetParameters.jsp")
    }

    override fun getDefaultRunnerProperties() = emptyMap<String, String>()

    override fun describeParameters(parameters: Map<String, String>): String {
        val paths = (parameters[DotnetConstants.PARAM_PATHS] ?: "").trim()
        val commandName = parameters[DotnetConstants.PARAM_COMMAND]?.replace('-', ' ')
        val args = parameters[DotnetConstants.PARAM_ARGUMENTS]?.let {
            StringUtil.splitCommandArgumentsAndUnquote(it).take(2).joinToString(" ")
        } ?: ""

        return when {
            commandName == DotnetCommandType.Custom.id -> "$paths\nCommand line parameters: $args"
            !commandName.isNullOrBlank() -> "$commandName $paths"
            else -> args
        }
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>): List<Requirement> {
        val requirements = arrayListOf<Requirement>()
        if (!isDocker(runParameters)) {
            runParameters[DotnetConstants.PARAM_COMMAND]?.let {
                DotnetParametersProvider.commandTypes[it]?.let {
                    requirements.addAll(it.getRequirements(runParameters, _factory))
                }
            }

            runParameters[CoverageConstants.PARAM_TYPE]?.let {
                DotnetParametersProvider.coverageTypes[it]?.let {
                    requirements.addAll(it.getRequirements(runParameters, _factory))
                }
            }
        }

        return requirements
    }

    private fun isDocker(parameters: Map<String, String>) = !parameters[DotnetConstants.PARAM_DOCKER_IMAGE].isNullOrEmpty()
}
