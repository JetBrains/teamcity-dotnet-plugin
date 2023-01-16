/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.commands.targeting.TargetTypeProvider
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import java.io.File

class TestCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    override val toolResolver: DotnetToolResolver,
    private val _targetService: TargetService,
    private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
    private val _assemblyArgumentsProvider: DotnetCommonArgumentsProvider,
    private val _dotnetFilterFactory: DotnetFilterFactory,
    private val _loggerService: LoggerService,
    private val _targetTypeProvider: TargetTypeProvider,
    private val _targetArgumentsProvider: TargetArgumentsProvider,
) : DotnetCommandBase(_parametersService) {
    override val commandType = DotnetCommandType.Test

    override val commandWords = sequenceOf("test")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetArgumentsProvider.getTargetArguments(_targetService.targets)

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val filter = _dotnetFilterFactory.createFilter(commandType);
        if (filter.isSplitting) {
            _loggerService.writeStandardOutput(DotnetConstants.PARALLEL_TESTS_FEATURE_REQUIREMENTS_MESSAGE)
        }

        if (filter.filter.isNotBlank()) {
            yield(CommandLineArgument("--filter"))
            yield(CommandLineArgument(filter.filter))
        }

        if (filter.settingsFile != null) {
            yield(CommandLineArgument("--settings"))
            yield(CommandLineArgument(filter.settingsFile.path))
        }
        else {
            parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--settings"))
                    yield(CommandLineArgument(it))
                }
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

        parameters(DotnetConstants.PARAM_OUTPUT_DIR)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--output"))
                yield(CommandLineArgument(it))
            }
        }

        if (parameters(DotnetConstants.PARAM_SKIP_BUILD, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-build"))
        }

        if (context.command.targetArguments.flatMap { it.arguments }.any { isAssembly(it.value)}) {
            yieldAll(_assemblyArgumentsProvider.getArguments(context))
        }
        else {
            yieldAll(_commonArgumentsProvider.getArguments(context))
        }
    }

    private fun isAssembly(path: String) = _targetTypeProvider.getTargetType(File(path)) == CommandTargetType.Assembly
}