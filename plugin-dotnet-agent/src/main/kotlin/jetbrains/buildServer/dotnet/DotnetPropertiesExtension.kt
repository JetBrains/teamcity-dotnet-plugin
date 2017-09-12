/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

/**
 * Provides a list of available dotnet cli runtimes.
 */
class DotnetPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _defaultEnvironmentVariables: EnvironmentVariables)
    : AgentLifeCycleAdapter() {
    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating .NET CLI tools")
        val command = CommandLine(
                TargetType.Tool,
                File(_toolProvider.getPath(DotnetConstants.RUNNER_TYPE)),
                File("."),
                listOf(CommandLineArgument("--version")),
                emptyList())

        try {
            _commandLineExecutor.tryExecute(command)?.let {
                _versionParser.tryParse(it.standardOutput)?.let {
                    agent.configuration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, it)
                    agent.configuration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, command.executableFile.absolutePath)
                    LOG.info("Found .NET CLI at ${command.executableFile.absolutePath}")
                }
            }
        } catch (e: ToolCannotBeFoundException) {
            LOG.debug(e)
            return
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetPropertiesExtension::class.java.name)
    }
}