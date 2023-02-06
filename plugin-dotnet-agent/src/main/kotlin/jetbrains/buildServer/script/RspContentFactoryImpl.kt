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

package jetbrains.buildServer.script

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class RspContentFactoryImpl(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _scriptProvider: ScriptResolver,
        private val _virtualContext: VirtualContext)
    : RspContentFactory {
    override fun create() = sequence {
        parameters(ScriptConstants.NUGET_PACKAGE_SOURCES)?.let {
            _argumentsService.split(it).forEach {
                yield("--source")
                yield(_virtualContext.resolvePath(it))
            }
        }

        for (paramName in _parametersService.getParameterNames(ParameterType.System)) {
            _parametersService.tryGetParameter(ParameterType.System, paramName)?.let {
                yield("--property")
                yield("$paramName=${_virtualContext.resolvePath(it)}")
            }
        }

        yield("--")

        yield(_virtualContext.resolvePath(_scriptProvider.resolve().path))

        parameters(ScriptConstants.ARGS)?.let {
            _argumentsService.split(it).forEach {
                yield(it)
            }
        }
    }

    protected fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}