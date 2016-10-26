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
 * Provides arguments to dotnet restore command.
 */
class RestoreArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.add(DotnetConstants.COMMAND_RESTORE)

        val projectsValue = parameters[DotnetConstants.PARAM_PATHS]
        if (!projectsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue!!))
        }

        val packagesValue = parameters[DotnetConstants.PARAM_RESTORE_PACKAGES]
        if (!packagesValue.isNullOrBlank()) {
            arguments.add("--packages")
            arguments.add(packagesValue!!.trim())
        }

        val sourceValue = parameters[DotnetConstants.PARAM_RESTORE_SOURCE]
        if (!sourceValue.isNullOrBlank()) {
            val sources = StringUtil.splitCommandArgumentsAndUnquote(sourceValue!!)
            sources.forEach {
                arguments.add("--source")
                arguments.add(it)
            }
        }

        val parallelValue = parameters[DotnetConstants.PARAM_RESTORE_PARALLEL]
        if ("true".equals(parallelValue, ignoreCase = true)) {
            arguments.add("--disable-parallel")
        }

        val configValue = parameters[DotnetConstants.PARAM_RESTORE_CONFIG]
        if (!configValue.isNullOrBlank()) {
            arguments.add("--configfile")
            arguments.add(configValue!!.trim())
        }

        val verbosityValue = parameters[DotnetConstants.PARAM_VERBOSITY]
        if (!verbosityValue.isNullOrBlank()) {
            arguments.add("--verbosity")
            arguments.add(verbosityValue!!.trim())
        }

        val argumentsValue = parameters[DotnetConstants.PARAM_ARGUMENTS]
        if (!argumentsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue!!))
        }

        return arguments
    }
}
