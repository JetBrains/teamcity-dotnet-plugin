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
 * Provides arguments to dotnet run command.
 */
class RunArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = arrayListOf(DotnetConstants.COMMAND_RUN)

        parameters[DotnetConstants.PARAM_PATHS]?.trim()?.let {
            arguments.addAll(listOf("--project", it))
        }

        parameters[DotnetConstants.PARAM_RUN_FRAMEWORK]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--framework", it))
            }
        }

        parameters[DotnetConstants.PARAM_RUN_CONFIG]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--configuration", it))
            }
        }

        parameters[DotnetConstants.PARAM_ARGUMENTS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        return arguments
    }
}
