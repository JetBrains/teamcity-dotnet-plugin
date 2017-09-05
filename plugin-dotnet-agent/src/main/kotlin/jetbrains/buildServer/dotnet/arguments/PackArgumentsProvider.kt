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
 * Provides arguments to dotnet pack command.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class PackArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _projectService: TargetService)
    : DotnetCommandArgumentsProvider {

    override val command: DotnetCommand
        get() = DotnetCommand.Pack

    override val targetArguments: Sequence<TargetArguments>
        get() = _projectService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_PACK_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--configuration"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_PACK_OUTPUT)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--output"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_PACK_TEMP)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--build-base-path"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_PACK_VERSION_SUFFIX)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--version-suffix"))
                    yield(CommandLineArgument(it))
                }
            }

            if (parameters(DotnetConstants.PARAM_PACK_NO_BUILD, "").trim().toBoolean()) {
                yield(CommandLineArgument("--no-build"))
            }

            if (parameters(DotnetConstants.PARAM_PACK_SERVICEABLE, "").trim().toBoolean()) {
                yield(CommandLineArgument("--serviceable"))
            }
        }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}
