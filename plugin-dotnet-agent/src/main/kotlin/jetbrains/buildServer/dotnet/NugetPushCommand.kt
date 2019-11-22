/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.rx.Observer

class NugetPushCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _targetService: TargetService,
        private val _customArgumentsProvider: ArgumentsProvider,
        override val toolResolver: DotnetToolResolver,
        private val _resultsObserver: Observer<CommandResultEvent>)
    : DotnetCommandBase(_parametersService, _resultsObserver) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.NuGetPush

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
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

        if (parameters(DotnetConstants.PARAM_NUGET_NO_SYMBOLS, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-symbols"))
            yield(CommandLineArgument("true"))
        }

        yield(CommandLineArgument("--force-english-output", CommandLineArgumentType.Infrastructural))

        yieldAll(_customArgumentsProvider.getArguments(context))
    }


}
