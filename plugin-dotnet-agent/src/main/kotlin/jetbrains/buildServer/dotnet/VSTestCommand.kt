/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.StringUtil

class VSTestCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _targetService: TargetService,
        private val _vstestLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        override val toolResolver: ToolResolver,
        private val _argumentsAlternative: ArgumentsAlternative,
        private val _testsFilterProvider: TestsFilterProvider,
        private val _splittedTestsFilterSettings: SplittedTestsFilterSettings,
        private val _loggerService: LoggerService,
        private val _targetArgumentsProvider: TargetArgumentsProvider)
    : DotnetCommandBase(_parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.VSTest

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetArgumentsProvider.getTargetArguments(_targetService.targets)

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("/Settings:$it"))
            }
        }

        var filterArgs: MutableList<CommandLineArgument> = mutableListOf();
        when (parameters(DotnetConstants.PARAM_TEST_FILTER)) {
            "filter" -> {
                _testsFilterProvider.filterExpression.let {
                    if (it.isNotBlank()) {
                        filterArgs.add(CommandLineArgument("/TestCaseFilter:$it"))
                    }
                }
            }
            "name" -> {
                if(_splittedTestsFilterSettings.IsActive) {
                    _loggerService.writeWarning("The \"Split tests by parallel groups\" feature is not supported together with a test names filter. Please consider using a test case filter.")
                }

                parameters(DotnetConstants.PARAM_TEST_NAMES)?.trim()?.let {
                    if (it.isNotBlank()) {
                        filterArgs.add(CommandLineArgument("/Tests:${StringUtil.split(it).joinToString(",")}"))
                    }
                }
            }
        }

        if (context.toolVersion >= VersionSupportingArgFiles) {
            yieldAll(_argumentsAlternative.select("Filter", filterArgs, filterArgs.asSequence(), emptySequence(), context.verbosityLevel))
        }
        else {
            yieldAll(filterArgs)
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

    companion object {
        internal val VersionSupportingArgFiles = Version(2, 1)
    }
}