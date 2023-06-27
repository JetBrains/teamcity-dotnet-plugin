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
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver

class ListTestsCommand(
    parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    override val toolResolver: DotnetToolResolver,
    private val _targetService: TargetService,
    private val _targetArgumentsProvider: TargetArgumentsProvider,
) : DotnetCommandBase(parametersService) {
    override val commandType = DotnetCommandType.ListTests

    override val command = sequenceOf("test")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetArgumentsProvider.getTargetArguments(_targetService.targets)

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("--list-tests", CommandLineArgumentType.Mandatory))

        yieldAll(
            sequenceOf(
                "--",
                // NUnit should be set up to print fully qualified names (xUnit & MSTest ignore this option)
                // xUnit test adapter prints fully qualified names by default
                // Unfortunately, there is no official way to make MSTest print FQN :(
                "NUnit.DisplayName=FullName",
            ).map(::CommandLineArgument)
        )
    }
}
