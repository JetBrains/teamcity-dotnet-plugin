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
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter
import jetbrains.buildServer.dotnet.commands.responseFile.ResponseFileFactory
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.commands.targeting.TargetsParser

class MSBuildCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _targetService: TargetService,
    private val _msBuildResponseFileArgumentsProvider: ArgumentsProvider,
    private val _customArgumentsProvider: ArgumentsProvider,
    override val toolResolver: ToolResolver,
    private val _vstestLoggerEnvironment: EnvironmentBuilder,
    private val _targetsParser: TargetsParser,
    private val _dotnetFilterFactory: DotnetFilterFactory,
    private val _responseFileFactory: ResponseFileFactory,
) : DotnetCommandBase(_parametersService) {

    override val commandType = DotnetCommandType.MSBuild

    override val command = sequenceOf("msbuild")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val filter = _dotnetFilterFactory.createFilter(commandType);

        parameters(DotnetConstants.PARAM_TARGETS)?.trim()?.let {
            val targets = _targetsParser.parse(it)
            if (targets.isNotBlank()) {
                yield(CommandLineArgument("-t:$targets"))
            }
        }

        parameters(DotnetConstants.PARAM_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("-p:Configuration=$it"))
            }
        }

        parameters(DotnetConstants.PARAM_PLATFORM)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("-p:Platform=$it"))
            }
        }

        parameters(DotnetConstants.PARAM_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("-p:RuntimeIdentifiers=$it"))
            }
        }

        context.verbosityLevel?.let {
            yield(CommandLineArgument("-v:${it.id.lowercase()}"))
        }

        yieldAll(_msBuildResponseFileArgumentsProvider.getArguments(context))

        if (filter.isSplittingByFilter)
        {
            val msBuldParams = mutableListOf<MSBuildParameter>()
            if (filter.filter.isNotBlank()) {
                msBuldParams.add(MSBuildParameter("VSTestTestCaseFilter", filter.filter))
            }

            if (filter.settingsFile != null) {
                msBuldParams.add(MSBuildParameter("VSTestSetting", filter.settingsFile.path))
            }

            if (msBuldParams.any()) {
                val filterRespponseFile = _responseFileFactory.createResponeFile("Filter", emptySequence(), msBuldParams.asSequence(), context.verbosityLevel)
                yield(CommandLineArgument("@${filterRespponseFile.path}"))
            }
        }

        yieldAll(_customArgumentsProvider.getArguments(context))
    }

    override val environmentBuilders: Sequence<EnvironmentBuilder>
        get() = sequence { yield(_vstestLoggerEnvironment) }
}