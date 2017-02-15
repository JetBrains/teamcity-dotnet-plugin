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
 * Provides arguments to dotnet restore command.
 */
class RestoreArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = arrayListOf(DotnetConstants.COMMAND_RESTORE)

        parameters[DotnetConstants.PARAM_PATHS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        parameters[DotnetConstants.PARAM_RESTORE_PACKAGES]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--packages", it))
            }
        }

        parameters[DotnetConstants.PARAM_RESTORE_SOURCE]?.let {
            StringUtil.splitCommandArgumentsAndUnquote(it).forEach {
                arguments.addAll(listOf("--source", it))
            }
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_RESTORE_PARALLEL, { "" }).trim().toBoolean()) {
            arguments.add("--disable-parallel")
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_RESTORE_NO_CACHE, { "" }).trim().toBoolean()) {
            arguments.add("--no-cache")
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_RESTORE_IGNORE_FAILED, { "" }).trim().toBoolean()) {
            arguments.add("--ignore-failed-sources")
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_RESTORE_ROOT_PROJECT, { "" }).trim().toBoolean()) {
            arguments.add("--no-dependencies")
        }

        parameters[DotnetConstants.PARAM_RESTORE_CONFIG]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--configfile", it))
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
