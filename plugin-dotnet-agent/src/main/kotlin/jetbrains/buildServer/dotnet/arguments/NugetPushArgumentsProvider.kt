/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet nuget push command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class NugetPushArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _projectService: TargetService)
    : DotnetCommandArgumentsProvider {

    override val command: DotnetCommand
        get() = DotnetCommand.NuGetPush

    override val targetArguments: Sequence<TargetArguments>
        get() = _projectService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        parameters(DotnetConstants.PARAM_NUGET_PUSH_API_KEY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--api-key"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_PUSH_SOURCE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--source"))
                yield(CommandLineArgument(it))
            }
        }

        if (parameters(DotnetConstants.PARAM_NUGET_PUSH_NO_SYMBOLS, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-symbols"))
            yield(CommandLineArgument("true"))
        }

        if (parameters(DotnetConstants.PARAM_NUGET_PUSH_NO_BUFFER, "").trim().toBoolean()) {
            yield(CommandLineArgument("--disable-buffering"))
            yield(CommandLineArgument("true"))
        }
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}
