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
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class RunCommand(
        private val _parametersService: ParametersService,
        private val _projectService: TargetService)
    : DotnetCommand {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Run

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
        }

    override fun isSuccess(exitCode: Int): Boolean = true

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}