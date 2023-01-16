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

package jetbrains.buildServer.dotnet.commands.vstest

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.logging.LoggerResolver

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class VSTestLoggerArgumentsProvider(
    private val _loggerResolver: LoggerResolver,
    private val _loggerParameters: LoggerParameters,
    private val _virtualContext: VirtualContext)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            yield(CommandLineArgument("/logger:logger://teamcity", CommandLineArgumentType.Infrastructural))
            yield(CommandLineArgument("/TestAdapterPath:${_virtualContext.resolvePath(it.canonicalPath)}", CommandLineArgumentType.Infrastructural))
            yield(CommandLineArgument("/logger:console;verbosity=${_loggerParameters.vsTestVerbosity.id.lowercase()}", CommandLineArgumentType.Infrastructural))
        }
    }
}