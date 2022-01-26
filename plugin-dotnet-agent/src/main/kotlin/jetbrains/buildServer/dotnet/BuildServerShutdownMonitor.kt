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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.agent.Logger

class BuildServerShutdownMonitor(
        eventSources: EventSources,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _parametersService: ParametersService,
        private val _environmentVariables: EnvironmentVariables,
        private val _virtualContext: VirtualContext)
    : CommandRegistry {

    private var _subscriptionToken: Disposable
    private var _workingDirectories = mutableMapOf<Version, Path>()
    internal val count get() = _workingDirectories.size

    init {
        _subscriptionToken = eventSources.buildFinishedSource.subscribe {
            if (_workingDirectories.isNotEmpty()) {
                try {
                    LOG.debug("Shared compilation service shutdown.")
                    val executable = _dotnetToolResolver.executable
                    for ((sdkVersion, workingDirectory) in _workingDirectories) {
                        val envVariables = _environmentVariables.getVariables(sdkVersion).toList()
                        _commandLineExecutor.tryExecute(
                                CommandLine(
                                        null,
                                        TargetType.Tool,
                                        executable.path,
                                        workingDirectory,
                                        shutdownArgs,
                                        envVariables)
                        )
                    }
                } finally {
                    _workingDirectories.clear()
                }
            }
        }
    }

    override fun register(context: DotnetBuildContext) {
        if (
                !_virtualContext.isVirtual
                && buildCommands.contains(context.command.commandType)
                && context.toolVersion > Version.LastVersionWithoutSharedCompilation
                && _parametersService.tryGetParameter(ParameterType.Environment, EnvironmentVariablesImpl.UseSharedCompilationEnvVarName)?.equals("true", true) ?: true) {
            _workingDirectories.getOrPut(context.toolVersion) { context.workingDirectory.path }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(BuildServerShutdownMonitor::class.java)
        internal val shutdownArgs = listOf(CommandLineArgument("build-server", CommandLineArgumentType.Mandatory), CommandLineArgument("shutdown", CommandLineArgumentType.Mandatory))
        private val buildCommands = setOf(
                DotnetCommandType.Build,
                DotnetCommandType.Pack,
                DotnetCommandType.Publish,
                DotnetCommandType.Test,
                DotnetCommandType.Run,
                DotnetCommandType.MSBuild)
    }
}