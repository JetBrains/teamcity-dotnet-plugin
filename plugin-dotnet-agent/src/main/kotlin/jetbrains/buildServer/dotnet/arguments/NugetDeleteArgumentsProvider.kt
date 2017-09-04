/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import jetbrains.buildServer.util.StringUtil
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet nuget delete command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class NugetDeleteArgumentsProvider(
        private val _parametersService: ParametersService)
    : DotnetCommandArgumentsProvider {

    override val command: DotnetCommand
        get() = DotnetCommand.NuGetDelete

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        parameters(DotnetConstants.PARAM_NUGET_DELETE_ID)?.trim()?.let {
            if (it.isNotBlank()) {
                yieldAll(jetbrains.buildServer.util.StringUtil.split(it).map{CommandLineArgument(it)})
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_DELETE_API_KEY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--api-key"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_DELETE_SOURCE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--source"))
                yield(CommandLineArgument(it))
            }
        }

        yield(CommandLineArgument("--non-interactive"))
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}