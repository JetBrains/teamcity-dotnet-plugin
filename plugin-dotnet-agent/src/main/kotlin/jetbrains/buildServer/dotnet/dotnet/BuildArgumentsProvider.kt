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
 * Provides arguments to dotnet build command.
 */
class BuildArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.add(DotnetConstants.COMMAND_BUILD)

        val projectsValue = parameters[DotnetConstants.PARAM_PATHS]
        if (!projectsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue!!))
        }

        val frameworkValue = parameters[DotnetConstants.PARAM_BUILD_FRAMEWORK]
        if (!frameworkValue.isNullOrBlank()) {
            arguments.add("--framework")
            arguments.add(frameworkValue!!.trim())
        }

        val configValue = parameters[DotnetConstants.PARAM_BUILD_CONFIG]
        if (!configValue.isNullOrBlank()) {
            arguments.add("--configuration")
            arguments.add(configValue!!.trim())
        }

        val runtimeValue = parameters[DotnetConstants.PARAM_BUILD_RUNTIME]
        if (!runtimeValue.isNullOrBlank()) {
            arguments.add("--runtime")
            arguments.add(runtimeValue!!.trim())
        }

        val buildProfileValue = parameters[DotnetConstants.PARAM_BUILD_PROFILE]
        if ("true".equals(buildProfileValue, ignoreCase = true)) {
            arguments.add("--build-profile")
        }

        val nonIncrementalValue = parameters[DotnetConstants.PARAM_BUILD_NON_INCREMENTAL]
        if ("true".equals(nonIncrementalValue, ignoreCase = true)) {
            arguments.add("--no-incremental")
        }

        val noDependenciesValue = parameters[DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES]
        if ("true".equals(noDependenciesValue, ignoreCase = true)) {
            arguments.add("--no-dependencies")
        }

        val outputValue = parameters[DotnetConstants.PARAM_BUILD_OUTPUT]
        if (!outputValue.isNullOrBlank()) {
            arguments.add("--output")
            arguments.add(outputValue!!.trim())
        }

        val tempValue = parameters[DotnetConstants.PARAM_BUILD_TEMP]
        if (!tempValue.isNullOrBlank()) {
            arguments.add("--build-base-path")
            arguments.add(tempValue!!.trim())
        }

        val argumentsValue = parameters[DotnetConstants.PARAM_ARGUMENTS]
        if (!argumentsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue!!))
        }

        return arguments
    }
}
