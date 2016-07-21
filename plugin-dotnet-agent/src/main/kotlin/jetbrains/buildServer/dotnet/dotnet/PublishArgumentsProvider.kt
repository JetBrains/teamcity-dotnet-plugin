/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.dotnet

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.StringUtil

import java.util.ArrayList

/**
 * Provides arguments to dotnet publish command.
 */
class PublishArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.add(DotnetConstants.COMMAND_PUBLISH)

        val projectsValue = parameters[DotnetConstants.PARAM_PATHS]
        if (!projectsValue.isNullOrBlank()) {
            arguments.add(projectsValue!!.trim())
        }

        val frameworkValue = parameters[DotnetConstants.PARAM_PUBLISH_FRAMEWORK]
        if (!frameworkValue.isNullOrBlank()) {
            arguments.add("--framework")
            arguments.add(frameworkValue!!.trim())
        }

        val configValue = parameters[DotnetConstants.PARAM_PUBLISH_CONFIG]
        if (!configValue.isNullOrBlank()) {
            arguments.add("--configuration")
            arguments.add(configValue!!.trim())
        }

        val runtimeValue = parameters[DotnetConstants.PARAM_PUBLISH_RUNTIME]
        if (!runtimeValue.isNullOrBlank()) {
            arguments.add("--runtime")
            arguments.add(runtimeValue!!.trim())
        }

        val outputValue = parameters[DotnetConstants.PARAM_PUBLISH_OUTPUT]
        if (!outputValue.isNullOrBlank()) {
            arguments.add("--output")
            arguments.add(outputValue!!.trim())
        }

        val tempValue = parameters[DotnetConstants.PARAM_PUBLISH_TEMP]
        if (!tempValue.isNullOrBlank()) {
            arguments.add("--build-base-path")
            arguments.add(tempValue!!.trim())
        }

        val noBuildValue = parameters[DotnetConstants.PARAM_PUBLISH_NO_BUILD]
        if ("true".equals(noBuildValue, ignoreCase = true)) {
            arguments.add("--no-build")
        }

        val versionSuffixValue = parameters[DotnetConstants.PARAM_PUBLISH_VERSION_SUFFIX]
        if (!versionSuffixValue.isNullOrBlank()) {
            arguments.add("--version-suffix")
            arguments.add(versionSuffixValue!!.trim())
        }

        val argumentsValue = parameters[DotnetConstants.PARAM_ARGUMENTS]
        if (!argumentsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue!!))
        }

        return arguments
    }
}
