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
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class DotnetCommonArgumentsProviderImpl(
        private val _avoidUsingRspFiles: Boolean,
        private val _responseFileArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _argumentsProviders: List<ArgumentsProvider>)
    : DotnetCommonArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val avoidUsingRspFiles = _avoidUsingRspFiles
        if (!avoidUsingRspFiles) {
            yieldAll(_responseFileArgumentsProvider.getArguments(context))
        } else {
            for (argumentsProvider in _argumentsProviders) {
                yieldAll(argumentsProvider.getArguments(context))
            }
        }

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}