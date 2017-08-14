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
 * Provides arguments to dotnet publish command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class PublishArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProvider {

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        yield(CommandLineArgument(DotnetConstants.COMMAND_PUBLISH))

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            yieldAll(_argumentsService.parseToStrings(it).map { CommandLineArgument(it) })
        }

        parameters(DotnetConstants.PARAM_PUBLISH_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--framework"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configuration"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--runtime"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_OUTPUT)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--output"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_VERSION_SUFFIX)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--version-suffix"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--verbosity"))
                yield(CommandLineArgument(it))
            }
        }
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

}
