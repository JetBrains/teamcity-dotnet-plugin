/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor

/**
 * Dotnet runner definition.
 */
class DotnetRunnerRunType(private val pluginDescriptor: PluginDescriptor,
                          runTypeRegistry: RunTypeRegistry) : RunType() {

    init {
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

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        return PropertiesProcessor { emptyList() }
    }

    override fun getEditRunnerParamsJspFilePath(): String? {
        return pluginDescriptor.getPluginResourcesPath("editDotnetParameters.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String? {
        return pluginDescriptor.getPluginResourcesPath("viewDotnetParameters.jsp")
    }

    override fun getDefaultRunnerProperties(): Map<String, String>? {
        return emptyMap()
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        val paths = parameters[DotnetConstants.PARAM_PATHS] ?: StringUtil.EMPTY
        return "dotnet ${parameters[DotnetConstants.PARAM_COMMAND]} $paths"
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>): List<Requirement> {
        return listOf(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
    }
}
