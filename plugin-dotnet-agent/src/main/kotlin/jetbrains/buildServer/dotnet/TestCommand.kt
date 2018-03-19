/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

class TestCommand(
        parametersService: ParametersService,
        private val _testsResultsAnalyzer: TestsResultsAnalyzer,
        private val _targetService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _vstestLoggerEnvironment: EnvironmentBuilder)
    : DotnetCommandBase(parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Test

    override val toolResolver: ToolResolver
        get() = _dotnetToolResolver

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.targetFile.path))) }

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_TEST_CASE_FILTER)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--filter"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_FRAMEWORK)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--framework"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--configuration"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--settings"))
                    yield(CommandLineArgument(it))
                }
            }

            parameters(DotnetConstants.PARAM_OUTPUT_DIR)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--output"))
                    yield(CommandLineArgument(it))
                }
            }

            if (parameters(DotnetConstants.PARAM_SKIP_BUILD, "").trim().toBoolean()) {
                yield(CommandLineArgument("--no-build"))
            }

            yieldAll(_commonArgumentsProvider.arguments)
        }

    override fun isSuccessful(result: CommandLineResult) =
            _testsResultsAnalyzer.isSuccessful(result)

    override val environmentBuilders: Sequence<EnvironmentBuilder>
        get() = buildSequence { yield(_vstestLoggerEnvironment) }
}