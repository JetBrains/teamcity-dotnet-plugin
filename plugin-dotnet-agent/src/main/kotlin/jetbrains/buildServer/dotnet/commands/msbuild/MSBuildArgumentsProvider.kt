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

package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetBuildContext

class MSBuildArgumentsProvider(
    private val _msBuildParameterConverter: MSBuildParameterConverter,
    private val _msBuildParametersProviders: List<MSBuildParametersProvider>)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> =
            _msBuildParametersProviders.asSequence().flatMap {
                _msBuildParameterConverter
                        .convert(it.getParameters(context))
                        .map { CommandLineArgument(it) }
            }

}