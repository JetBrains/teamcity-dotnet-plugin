/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.SimpleCommandLineProcessRunner
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher

/**
 * Provides a list of available dotnet cli runtimes.
 */
class DotnetPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _toolProvider: DotnetToolProvider,
        private val _dotnetSdkVersionProvider: DotnetSdkVersionProvider,
        private val _defaultEnvironmentVariables: EnvironmentVariables) : AgentLifeCycleAdapter() {

    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        val config = agent.configuration
        val toolPath: String
        val version: String

        LOG.info("Locating .NET CLI tools")
        try {
            toolPath = _toolProvider.getPath(DotnetConstants.RUNNER_TYPE)
            val commandLine = getVersionCommandLine(toolPath)
            val result = SimpleCommandLineProcessRunner.runCommand(commandLine, byteArrayOf())
            version = _dotnetSdkVersionProvider.getSdkVersion(result.stdout)
        } catch (e: ToolCannotBeFoundException) {
            LOG.debug(e)
            return
        }

        LOG.info("Found .NET CLI at $toolPath")
        config.addConfigurationParameter(DotnetConstants.CONFIG_NAME, version)
        config.addConfigurationParameter(DotnetConstants.CONFIG_PATH, toolPath)
    }

    private fun getVersionCommandLine(toolPath: String): GeneralCommandLine {
        val commandLine = GeneralCommandLine()
        commandLine.exePath = toolPath
        commandLine.addParameter("--version")
        var environmentVariables = System.getenv().toMutableMap()
        _defaultEnvironmentVariables.variables.forEach { environmentVariables[it.name] = it.value }
        commandLine.envParams = environmentVariables
        return commandLine
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetPropertiesExtension::class.java.name)
    }
}