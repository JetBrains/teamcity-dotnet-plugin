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
 * Provides arguments to dotnet run command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RunArgumentsProvider(
        private val _parametersService: ParametersService)
    : ArgumentsProvider {

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        yield(CommandLineArgument(DotnetConstants.COMMAND_RUN))

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            yield(CommandLineArgument("--project"))
            yield(CommandLineArgument(it))
        }

        parameters(DotnetConstants.PARAM_RUN_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--framework"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_RUN_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configuration"))
                yield(CommandLineArgument(it))
            }
        }
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}