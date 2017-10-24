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

class PackCommand(
        parametersService: ParametersService,
        private val _targetService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetCommandBase(parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Pack

    override val toolResolver: ToolResolver
        get() = _dotnetToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_PACK_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--configuration"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_PACK_RUNTIME)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--runtime"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_PACK_OUTPUT)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--output"))
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

            yieldAll(_commonArgumentsProvider.arguments)
        }
}
