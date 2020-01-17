/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_RSP

class DotnetCommonArgumentsProviderImpl(
        private val _parametersService: ParametersService,
        private val _responseFileArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _msBuildVSTestLoggerParametersProvider: MSBuildParametersProvider,
        private val _msBuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _sharedCompilationArgumentsProvider: ArgumentsProvider,
        private val _msBuildParameterConverter: MSBuildParameterConverter)
    : DotnetCommonArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val avoidUsingRspFiles = _parametersService.tryGetParameter(ParameterType.Configuration, PARAM_RSP)?.equals("false", true) ?: false
        if (!avoidUsingRspFiles) {
            yieldAll(_responseFileArgumentsProvider.getArguments(context))
        } else {
            yieldAll(_msBuildLoggerArgumentsProvider.getArguments(context))
            yieldAll(_msBuildVSTestLoggerParametersProvider.getParameters(context).map { CommandLineArgument(_msBuildParameterConverter.convert(it)) })
            yieldAll(_sharedCompilationArgumentsProvider.getArguments(context))
        }

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}