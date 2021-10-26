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

import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class SystemParametersProvider(
        private val _parametersService: ParametersService,
        private val _virtualContext: VirtualContext)
    : MSBuildParametersProvider {
    override fun getParameters(context: DotnetBuildContext): Sequence<MSBuildParameter> = sequence {
        for (paramName in _parametersService.getParameterNames(ParameterType.System)) {
            _parametersService.tryGetParameter(ParameterType.System, paramName)?.let {
                yield(MSBuildParameter(paramName, _virtualContext.resolvePath(it)))
            }
        }
    }
}