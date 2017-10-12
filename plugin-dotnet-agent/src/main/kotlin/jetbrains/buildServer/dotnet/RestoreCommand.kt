/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

class RestoreCommand(
        parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _targetService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetCommandBase(parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Restore

    override val toolResolver: ToolResolver
        get() = _dotnetToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_RESTORE_PACKAGES)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--packages"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_RESTORE_SOURCE)?.let {
                _argumentsService.split(it).forEach {
                    yield(CommandLineArgument("--source"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_RESTORE_RUNTIME)?.let {
                _argumentsService.split(it).forEach {
                    yield(CommandLineArgument("--runtime"))
                    yield(CommandLineArgument(it))
                }
            }

            if (parameters(DotnetConstants.PARAM_RESTORE_PARALLEL, "").trim().toBoolean()) {
                yield(CommandLineArgument("--disable-parallel"))
            }

            if (parameters(DotnetConstants.PARAM_RESTORE_NO_CACHE, "").trim().toBoolean()) {
                yield(CommandLineArgument("--no-cache"))
            }

            if (parameters(DotnetConstants.PARAM_RESTORE_IGNORE_FAILED, "").trim().toBoolean()) {
                yield(CommandLineArgument("--ignore-failed-sources"))
            }

            if (parameters(DotnetConstants.PARAM_RESTORE_ROOT_PROJECT, "").trim().toBoolean()) {
                yield(CommandLineArgument("--no-dependencies"))
            }

            parameters(DotnetConstants.PARAM_RESTORE_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--configfile"))
                    yield(CommandLineArgument(it))
                }
            }

            yieldAll(_commonArgumentsProvider.arguments)
        }
}