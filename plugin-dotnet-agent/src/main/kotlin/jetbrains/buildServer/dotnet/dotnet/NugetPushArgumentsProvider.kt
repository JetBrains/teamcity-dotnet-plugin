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
 * Provides arguments to dotnet nuget push command.
 */
class NugetPushArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.addAll(StringUtil.split(DotnetConstants.COMMAND_NUGET_PUSH))

        parameters[DotnetConstants.PARAM_PATHS]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.add(it)
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

        if (parameters.getOrElse(DotnetConstants.PARAM_NUGET_PUSH_NO_SYMBOLS, { "" }).trim().toBoolean()) {
            arguments.addAll(listOf("--no-symbols", "true"))
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_NUGET_PUSH_NO_BUFFER, { "" }).trim().toBoolean()) {
            arguments.addAll(listOf("--disable-buffering", "true"))
        }

        parameters[DotnetConstants.PARAM_ARGUMENTS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        return arguments
    }
}
