/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RunCommand(
        private val _parametersService: ParametersService,
        private val _projectService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetCommand {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Run

    override val toolResolver: ToolResolver
        get() = _dotnetToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _projectService.targets.map { TargetArguments(sequenceOf(CommandLineArgument("--project"), CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
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

            yieldAll(_commonArgumentsProvider.arguments)
        }

    override fun isSuccessfulExitCode(exitCode: Int): Boolean = true

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}