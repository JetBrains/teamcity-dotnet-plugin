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
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.util.StringUtil

class NugetDeleteCommand(
        _parametersService: ParametersService,
        override val resultsAnalyzer: ResultsAnalyzer,
        private val _customArgumentsProvider: ArgumentsProvider,
        override val toolResolver: DotnetToolResolver,
        private val _resultsObserver: Observer<CommandResultEvent>)
    : DotnetCommandBase(_parametersService, _resultsObserver) {

    override val commandType: DotnetCommandType
        get() = DotnetCommandType.NuGetDelete

    override val targetArguments: Sequence<TargetArguments>
        get() = emptySequence()

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_NUGET_PACKAGE_ID)?.trim()?.let {
            if (it.isNotBlank()) {
                yieldAll(StringUtil.split(it).map { CommandLineArgument(it) })
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_API_KEY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--api-key"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_PACKAGE_SOURCE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--source"))
                yield(CommandLineArgument(it))
            }
        }

        yield(CommandLineArgument("--non-interactive", CommandLineArgumentType.Infrastructural))
        yield(CommandLineArgument("--force-english-output", CommandLineArgumentType.Infrastructural))

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}