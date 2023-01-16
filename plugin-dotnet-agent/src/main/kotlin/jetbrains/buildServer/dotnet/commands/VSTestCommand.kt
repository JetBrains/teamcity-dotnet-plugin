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
import jetbrains.buildServer.dotnet.DotnetConstants.PARALLEL_TESTS_FEATURE_REQUIREMENTS_MESSAGE
import jetbrains.buildServer.dotnet.DotnetConstants.PARALLEL_TESTS_FEATURE_NAME
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.util.StringUtil

class VSTestCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _targetService: TargetService,
    private val _vstestLoggerArgumentsProvider: ArgumentsProvider,
    private val _customArgumentsProvider: ArgumentsProvider,
    override val toolResolver: ToolResolver,
    private val _dotnetFilterFactory: DotnetFilterFactory,
    private val _loggerService: LoggerService,
    private val _targetArgumentsProvider: TargetArgumentsProvider,
) : DotnetCommandBase(_parametersService) {
    override val commandType = DotnetCommandType.VSTest

    override val commandWords = sequenceOf("vstest")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetArgumentsProvider.getTargetArguments(_targetService.targets)

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val filter = _dotnetFilterFactory.createFilter(commandType);
        if (filter.isSplitting) {
            _loggerService.writeStandardOutput(PARALLEL_TESTS_FEATURE_REQUIREMENTS_MESSAGE)
        }

        if (filter.settingsFile != null) {
            yield(CommandLineArgument("/Settings:${filter.settingsFile.path}"))
        }
        else {
            parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Settings:$it"))
                }
            }
        }

        if (parameters(DotnetConstants.PARAM_TEST_FILTER) == "name") {
            if (filter.isSplitting) {
                _loggerService.writeWarning("The \"$PARALLEL_TESTS_FEATURE_NAME\" feature is not supported together with a test names filter. Please consider using a test case filter.")
            }

            parameters(DotnetConstants.PARAM_TEST_NAMES)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("/Tests:${StringUtil.split(it).joinToString(",")}"))
                }
            }
        }
        else {
            if (filter.filter.isNotBlank()) {
                yield(CommandLineArgument("/TestCaseFilter:${filter.filter}"))
            }
        }

        parameters(DotnetConstants.PARAM_PLATFORM)?.trim()?.let {
            if (it.isNotBlank()) {
                Platform.tryParse(it)?.let {
                    if( it != Platform.Default) {
                        yield(CommandLineArgument("/Platform:${it.id}"))
                    }
                }
            }
        }

        parameters(DotnetConstants.PARAM_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("/Framework:$it"))
            }
        }

        if (parameters(DotnetConstants.PARAM_VSTEST_IN_ISOLATION, "").trim().toBoolean()) {
            yield(CommandLineArgument("/InIsolation"))
        }

        yieldAll(_vstestLoggerArgumentsProvider.getArguments(context))
        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}