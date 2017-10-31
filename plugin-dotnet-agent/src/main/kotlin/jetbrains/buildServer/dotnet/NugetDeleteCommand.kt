/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

class NugetDeleteCommand(
        parametersService: ParametersService,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetCommandBase(parametersService) {

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

            yieldAll(_customArgumentsProvider.arguments)
        }
}