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
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides a list of available .NET CLI parameters.
 */
class DotnetPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _fileSystemService: FileSystemService)
    : AgentLifeCycleAdapter(), DotnetCliToolInfo {
    init {
        events.addListener(this)
    }

    private var _version: Version = jetbrains.buildServer.dotnet.Version.Empty;

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.debug("Locating .NET CLI")
        try {
            val command = CommandLine(
                    TargetType.Tool,
                    File(_toolProvider.getPath(DotnetConstants.EXECUTABLE)),
                    File("."),
                    listOf(CommandLineArgument("--version")),
                    emptyList())

            _commandLineExecutor.tryExecute(command)?.let {
                _versionParser.tryParse(it.standardOutput)?.let {
                    val dotnetPath = command.executableFile
                    _version = jetbrains.buildServer.dotnet.Version.parse(it);
                    agent.configuration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, it)
                    LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_NAME}\": \"$it\"")
                    agent.configuration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, dotnetPath.absolutePath)
                    LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_PATH}\": \"${dotnetPath.absolutePath}\"")
                    LOG.info(".NET CLI $it found at \"${dotnetPath.absolutePath}\"")

                    LOG.debug("Locating .NET Core SDKs")

                    val sdks = _fileSystemService.list(File(dotnetPath.parentFile, "sdk"))
                            .filter { it.isDirectory }
                            .map { Sdk(it, jetbrains.buildServer.dotnet.Version.parse(it.name)) }
                            .filter { it.version != jetbrains.buildServer.dotnet.Version.Empty }

                    for (sdk in enumerateSdk(sdks)) {
                        val paramName = "${DotnetConstants.CONFIG_SDK_NAME}${sdk.version}${DotnetConstants.PATH_SUFFIX}"
                        val paramValue = sdk.path.absolutePath;
                        agent.configuration.addConfigurationParameter(paramName, paramValue)
                        LOG.debug("Add configuration parameter \"$paramName\": \"${paramValue}\"")
                        LOG.info(".NET Core SDK ${paramValue} found at \"${paramValue}\"")
                    }
                }
            }

        } catch (e: ToolCannotBeFoundException) {
            LOG.info(".NET CLI not found")
            LOG.debug(e)
        }
    }

    override val Version: Version get() = _version

    companion object {
        private val LOG = Logger.getInstance(DotnetPropertiesExtension::class.java.name)

        fun enumerateSdk(versions: Sequence<Sdk>): Sequence<Sdk> = buildSequence {
            for (majorVersionGroup in versions.groupBy { Version(*it.version.fullVersion.take(2).toIntArray()) }) {
                yield(Sdk(majorVersionGroup.value.maxBy { it.version }!!.path, majorVersionGroup.key))
                yieldAll(majorVersionGroup.value)
            }
        }
    }

    data class Sdk(val path :File, val version: Version) {}
}