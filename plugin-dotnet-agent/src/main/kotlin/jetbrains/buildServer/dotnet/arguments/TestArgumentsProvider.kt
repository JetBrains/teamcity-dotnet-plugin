/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet test command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class TestArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _vsTestLoggerArgumentsProvider: ArgumentsProvider)
    : ArgumentsProvider {

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        yield(CommandLineArgument(DotnetConstants.COMMAND_TEST))

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_TEST_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--framework"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_TEST_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configuration"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_TEST_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--runtime"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_TEST_OUTPUT)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--output"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_TEST_TEMP)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--build-base-path"))
                yield(CommandLineArgument(it))
            }
        }

        if (parameters(DotnetConstants.PARAM_TEST_NO_BUILD, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-build"))
        }

        yieldAll(_vsTestLoggerArgumentsProvider.getArguments())
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}