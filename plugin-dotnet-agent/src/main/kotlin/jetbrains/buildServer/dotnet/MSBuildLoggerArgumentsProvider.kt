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
import jetbrains.buildServer.agent.VirtualContext

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class MSBuildLoggerArgumentsProvider(
        private val _loggerResolver: LoggerResolver,
        private val _loggerParameters: LoggerParameters,
        private val _virtualContext: VirtualContext)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("/noconsolelogger"))
        val parameters = sequence<String> {
            yield("/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${_virtualContext.resolvePath(_loggerResolver.resolve(ToolType.MSBuild).canonicalPath)}")
            yield("TeamCity")

            _loggerParameters.msBuildLoggerVerbosity?.let {
                yield("verbosity=${it.id.toLowerCase()}")
            }

            yieldAll(_loggerParameters.getAdditionalLoggerParameters(context))

            _loggerParameters.msBuildParameters.let {
                if (it.isNotBlank()) {
                    yield(it);
                }
            }
        }

        yield(CommandLineArgument("\"${parameters.joinToString(";")}\"", CommandLineArgumentType.Infrastructural))
    }
}