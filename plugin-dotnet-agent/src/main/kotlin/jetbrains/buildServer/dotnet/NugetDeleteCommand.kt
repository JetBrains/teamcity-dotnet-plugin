/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.StringUtil

class NugetDeleteCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _customArgumentsProvider: ArgumentsProvider,
        override val toolResolver: DotnetToolResolver)
    : DotnetCommandBase(_parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.NuGetDelete

    override val targetArguments: Sequence<TargetArguments>
        get() = emptySequence()

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_NUGET_PACKAGE_ID)?.trim()?.let {
            if (it.isNotBlank()) {
                yieldAll(StringUtil.split(it).map { CommandLineArgument(it) })
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_API_KEY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--api-key"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_PACKAGE_SOURCE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--source"))
                yield(CommandLineArgument(it))
            }
        }

        yield(CommandLineArgument("--non-interactive"))

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}