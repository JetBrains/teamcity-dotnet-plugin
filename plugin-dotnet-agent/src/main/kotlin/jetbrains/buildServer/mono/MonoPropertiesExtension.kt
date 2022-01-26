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

package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.agent.Logger

class MonoPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser)
    : AgentLifeCycleAdapter() {

    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.debug("Locating Mono")
        try {
            val command = CommandLine(
                    null,
                    TargetType.Tool,
                    Path(_toolProvider.getPath(MonoConstants.RUNNER_TYPE)),
                    Path("."),
                    listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
                    emptyList())
            _commandLineExecutor.tryExecute(command)?.let {
                val version = _versionParser.parse(it.standardOutput)
                if (version != Version.Empty) {
                    agent.configuration.addConfigurationParameter(MonoConstants.CONFIG_PATH, command.executableFile.path)
                    LOG.info("Found Mono $it at ${command.executableFile.path}")
                }
                else {
                    LOG.info("Mono not found")
                }
            }
        } catch (e: ToolCannotBeFoundException) {
            LOG.info("Mono not found")
            LOG.debug(e)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(MonoPropertiesExtension::class.java)
    }
}