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
class BuildCommand(
        private val _parametersService: ParametersService,
        private val _projectService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetCommand {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Build

    override val toolResolver: ToolResolver
        get() = _dotnetToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _projectService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_BUILD_FRAMEWORK)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--framework"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_BUILD_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--configuration"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_BUILD_RUNTIME)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--runtime"))
                    yield(CommandLineArgument(it))
                }
            }

            if (parameters(DotnetConstants.PARAM_BUILD_NON_INCREMENTAL, "").trim().toBoolean()) {
                yield(CommandLineArgument("--no-incremental"))
            }

            if (parameters(DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES, "").trim().toBoolean()) {
                yield(CommandLineArgument("--no-dependencies"))
            }

            parameters(DotnetConstants.PARAM_BUILD_OUTPUT)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--output"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_BUILD_VERSION_SUFFIX)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--version-suffix"))
                    yield(CommandLineArgument(it))
                }
            }

            yieldAll(_commonArgumentsProvider.arguments)
        }


    override fun isSuccessfulExitCode(exitCode: Int): Boolean = exitCode == 0

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}