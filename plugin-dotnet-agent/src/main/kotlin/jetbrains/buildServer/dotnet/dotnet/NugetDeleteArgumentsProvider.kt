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
 * Provides arguments to dotnet nuget delete command.
 */
class NugetDeleteArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.addAll(StringUtil.split(DotnetConstants.COMMAND_NUGET_DELETE))

        parameters[DotnetConstants.PARAM_NUGET_DELETE_ID]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(StringUtil.split(it))
            }
        }

        parameters[DotnetConstants.PARAM_NUGET_API_KEY]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--api-key", it))
            }
        }

        parameters[DotnetConstants.PARAM_NUGET_SOURCE]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--source", it))
            }
        }

        arguments.addAll(listOf("--non-interactive"))

        parameters[DotnetConstants.PARAM_ARGUMENTS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        return arguments
    }
}
