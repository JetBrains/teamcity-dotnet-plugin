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

        val projectsValue = parameters[DotnetConstants.PARAM_PATHS]
        if (!projectsValue.isNullOrBlank()) {
            arguments.add(projectsValue!!.trim())
        }

        val frameworkValue = parameters[DotnetConstants.PARAM_TEST_FRAMEWORK]
        if (!frameworkValue.isNullOrBlank()) {
            arguments.add("--framework")
            arguments.add(frameworkValue!!.trim())
        }

        val configValue = parameters[DotnetConstants.PARAM_TEST_CONFIG]
        if (!configValue.isNullOrBlank()) {
            arguments.add("--configuration")
            arguments.add(configValue!!.trim())
        }

        val runtimeValue = parameters[DotnetConstants.PARAM_TEST_RUNTIME]
        if (!runtimeValue.isNullOrBlank()) {
            arguments.add("--runtime")
            arguments.add(runtimeValue!!.trim())
        }

        val outputValue = parameters[DotnetConstants.PARAM_TEST_OUTPUT]
        if (!outputValue.isNullOrBlank()) {
            arguments.add("--output")
            arguments.add(outputValue!!.trim())
        }

        val tempValue = parameters[DotnetConstants.PARAM_TEST_TEMP]
        if (!outputValue.isNullOrBlank()) {
            arguments.add("--build-base-path")
            arguments.add(tempValue!!.trim())
        }

        val noBuildValue = parameters[DotnetConstants.PARAM_TEST_NO_BUILD]
        if ("true".equals(noBuildValue, ignoreCase = true)) {
            arguments.add("--no-build")
        }

        val argumentsValue = parameters[DotnetConstants.PARAM_ARGUMENTS]
        if (!argumentsValue.isNullOrBlank()) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue!!))
        }

        return arguments
    }
}
