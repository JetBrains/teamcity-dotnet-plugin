/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService

class CustomCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _customArgumentsProvider: ArgumentsProvider,
        override val toolResolver: DotnetToolResolver)
    : DotnetCommandBase(_parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Custom

    override val targetArguments: Sequence<TargetArguments>
        get() = emptySequence()

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = _customArgumentsProvider.getArguments(context)

    override val environmentBuilders: Sequence<EnvironmentBuilder>
        get() = emptySequence()
}