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

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class LoggerParametersImpl(
        private val _parametersService: ParametersService,
        private val _customArgumentsProvider: ArgumentsProvider)
    : LoggerParameters {

    override val paramVerbosity: Verbosity?
        get() = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                ?.trim()
                ?.let {
                    Verbosity.tryParse(it)
                }

    override val msBuildLoggerVerbosity: Verbosity?
        get() = paramVerbosity

    override val vsTestVerbosity: Verbosity
        get() = paramVerbosity?.let {
            when (it) {
                Verbosity.Quiet, Verbosity.Minimal -> Verbosity.Normal
                else -> it
            }
        }
                ?: Verbosity.Normal

    override val msBuildParameters: String
        get() = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MSBUILD_LOGGER_PARAMS) ?: defaultMsBuildLoggerParams

    override fun getAdditionalLoggerParameters(context: DotnetBuildContext) =
            _customArgumentsProvider
                    .getArguments(context)
                    .mapNotNull { LoggerParamRegex.find(it.value) }
                    .map { it.groupValues[2] }
                    .flatMap { it.split(';').asSequence() }
                    .filter { !it.isNullOrBlank() }

    companion object {
        const val defaultMsBuildLoggerParams = "plain";
        private val LoggerParamRegex = Regex("^\\s*([-/]consoleloggerparameters|[-/]clp):(.+?)\\s*\$", RegexOption.IGNORE_CASE)
    }
}