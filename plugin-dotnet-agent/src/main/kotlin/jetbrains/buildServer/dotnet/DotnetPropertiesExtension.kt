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
 * Provides a list of available .NET CLI parameters.
 */
class DotnetPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _semanticVersionParser: SemanticVersionParser,
        private val _fileSystemService: FileSystemService)
    : AgentLifeCycleAdapter() {
    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.debug("Locating .NET CLI")
        try {
            val command = CommandLine(
                    TargetType.Tool,
                    File(_toolProvider.getPath(DotnetConstants.RUNNER_TYPE)),
                    File("."),
                    listOf(CommandLineArgument("--version")),
                    emptyList())

            _commandLineExecutor.tryExecute(command)?.let {
                _versionParser.tryParse(it.standardOutput)?.let {
                    val dotnetPath = command.executableFile
                    agent.configuration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, it)
                    LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_NAME}\": \"$it\"")
                    agent.configuration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, dotnetPath.absolutePath)
                    LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_PATH}\": \"${dotnetPath.absolutePath}\"")
                    LOG.info(".NET CLI $it found at \"${dotnetPath.absolutePath}\"")

                    LOG.debug("Locating .NET Core SDKs")

                    _fileSystemService.list(File(dotnetPath.parentFile, "sdk")).forEach { file ->
                        if (file.isDirectory) {
                            _semanticVersionParser.tryParse(file.name)?.let {
                                val paramName = "${DotnetConstants.CONFIG_SDK_NAME}${it.major}.${it.minor}${DotnetConstants.PATH_SUFFIX}"
                                agent.configuration.addConfigurationParameter(paramName, file.absolutePath)
                                LOG.debug("Add configuration parameter \"$paramName\": \"${file.absolutePath}\"")
                                LOG.info(".NET Core SDK $it found at \"${file.absolutePath}\"")
                            }
                        }
                    }
                }
            }

        } catch (e: ToolCannotBeFoundException) {
            LOG.info(".NET CLI not found")
            LOG.debug(e)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetPropertiesExtension::class.java.name)
    }
}