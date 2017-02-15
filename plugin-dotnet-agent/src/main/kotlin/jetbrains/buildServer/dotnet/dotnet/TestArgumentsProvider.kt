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
 * Provides arguments to dotnet test command.
 */
class TestArgumentsProvider : ArgumentsProvider {

    override fun getArguments(parameters: Map<String, String>): List<String> {
        val arguments = ArrayList<String>()
        arguments.add(DotnetConstants.COMMAND_TEST)

        parameters[DotnetConstants.PARAM_PATHS]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.add(it)
            }
        }

        parameters[DotnetConstants.PARAM_TEST_FRAMEWORK]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--framework", it))
            }
        }

        parameters[DotnetConstants.PARAM_TEST_CONFIG]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--configuration", it))
            }
        }

        parameters[DotnetConstants.PARAM_TEST_RUNTIME]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--runtime", it))
            }
        }

        parameters[DotnetConstants.PARAM_TEST_OUTPUT]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--output", it))
            }
        }

        parameters[DotnetConstants.PARAM_TEST_TEMP]?.trim()?.let {
            if (it.isNotBlank()) {
                arguments.addAll(listOf("--build-base-path", it))
            }
        }

        if (parameters.getOrElse(DotnetConstants.PARAM_TEST_NO_BUILD, { "" }).trim().toBoolean()) {
            arguments.add("--no-build")
        }

        parameters[DotnetConstants.PARAM_ARGUMENTS]?.trim()?.let {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
        }

        return arguments
    }
}
