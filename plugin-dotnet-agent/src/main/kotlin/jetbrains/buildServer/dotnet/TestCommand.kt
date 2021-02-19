/*
 * Copyright 2000-2021 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class TestCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _targetService: TargetService,
        private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
        private val _assemblyArgumentsProvider: DotnetCommonArgumentsProvider,
        override val toolResolver: DotnetToolResolver,
        private val _vstestLoggerEnvironment: EnvironmentBuilder,
        private val _argumentsAlternative: ArgumentsAlternative)
    : DotnetCommandBase(_parametersService) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.Test

    override val targetArguments: Sequence<TargetArguments>
        get() =
            _targetService.targets.fold(mutableListOf<Pair<Boolean, MutableList<String>>>()) {
                acc, commandTarget ->
                val path = commandTarget.target.path
                val isAssembly = isAssembly(path)
                if (isAssembly && acc.any()) {
                    val group = acc.last()
                    if (group.first == isAssembly) {
                        group.second.add(path)
                        return@fold acc
                    }
                }

                acc.add(Pair(isAssembly, mutableListOf(path)))
                acc
            }.map {
                TargetArguments(it.second.asSequence().map { CommandLineArgument(it, CommandLineArgumentType.Target) })
            }.asSequence()

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_TEST_CASE_FILTER)?.trim()?.let {
            if (it.isNotBlank()) {
                yieldAll(
                        _argumentsAlternative.select(
                        "Filter",
                                listOf(CommandLineArgument("--filter"), CommandLineArgument(it)),
                                sequenceOf(MSBuildParameter("VSTestTestCaseFilter", it)),
                                context.verbosityLevel)
                )
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

        if (context.command.targetArguments.flatMap { it.arguments }.any { isAssembly(it.value)}) {
            yieldAll(_assemblyArgumentsProvider.getArguments(context))
        }
        else {
            yieldAll(_commonArgumentsProvider.getArguments(context))
        }
    }

    private fun isAssembly(path: String) = "dll".equals(File(path).extension, true)
}