/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import java.io.File
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class NugetDeleteCommand(
        private val _parametersService: ParametersService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetCommand {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.NuGetDelete

    override val toolResolver: ToolResolver
        get() = _dotnetToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = emptySequence()

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_NUGET_DELETE_ID)?.trim()?.let {
                if (it.isNotBlank()) {
                    yieldAll(jetbrains.buildServer.util.StringUtil.split(it).map { CommandLineArgument(it) })
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

            yieldAll(_commonArgumentsProvider.arguments)
        }

    override fun isSuccessfulExitCode(exitCode: Int): Boolean = exitCode == 0

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}