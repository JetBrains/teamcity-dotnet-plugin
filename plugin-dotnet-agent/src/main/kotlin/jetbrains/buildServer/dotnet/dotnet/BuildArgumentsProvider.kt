/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.dotnet

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.StringUtil

/**
 * Provides arguments to dotnet build command.
 */
class BuildArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = arrayListOf(DotnetConstants.COMMAND_BUILD)

        parameters[DotnetConstants.PARAM_PATHS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        parameters[DotnetConstants.PARAM_BUILD_FRAMEWORK]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--framework", it))
            }
        }

        parameters[DotnetConstants.PARAM_BUILD_CONFIG]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--configuration", it))
            }
        }

        parameters[DotnetConstants.PARAM_BUILD_RUNTIME]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--runtime", it))
            }
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_BUILD_NON_INCREMENTAL, { "" }).trim().toBoolean()) {
            arguments.add("--no-incremental")
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES, { "" }).trim().toBoolean()) {
            arguments.add("--no-dependencies")
        }

        parameters[DotnetConstants.PARAM_BUILD_OUTPUT]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--output", it))
            }
        }

        parameters[DotnetConstants.PARAM_BUILD_VERSION_SUFFIX]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--version-suffix", it))
            }
        }

        parameters[DotnetConstants.PARAM_VERBOSITY]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--verbosity", it))
            }
        }

        parameters[DotnetConstants.PARAM_ARGUMENTS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        return arguments
    }
}
