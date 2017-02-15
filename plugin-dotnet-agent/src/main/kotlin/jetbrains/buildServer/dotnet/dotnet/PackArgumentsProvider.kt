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

import java.util.ArrayList

/**
 * Provides arguments to dotnet pack command.
 */
class PackArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.add(DotnetConstants.COMMAND_PACK)

        parameters[DotnetConstants.PARAM_PATHS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        parameters[DotnetConstants.PARAM_PACK_CONFIG]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--configuration", it))
            }
        }

        parameters[DotnetConstants.PARAM_PACK_OUTPUT]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--output", it))
            }
        }

        parameters[DotnetConstants.PARAM_PACK_TEMP]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--build-base-path", it))
            }
        }

        parameters[DotnetConstants.PARAM_PACK_VERSION_SUFFIX]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--version-suffix", it))
            }
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_PACK_NO_BUILD, { "" }).trim().toBoolean()) {
            arguments.add("--no-build")
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_PACK_SERVICEABLE, { "" }).trim().toBoolean()) {
            arguments.add("--serviceable")
        }

        parameters[DotnetConstants.PARAM_ARGUMENTS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        return arguments
    }
}
