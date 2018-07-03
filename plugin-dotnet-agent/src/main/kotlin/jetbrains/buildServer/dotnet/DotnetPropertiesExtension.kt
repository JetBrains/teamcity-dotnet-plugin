/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import java.io.File
import kotlin.coroutines.experimental.buildSequence

/**`
 * Provides a list of available .NET CLI parameters.
 */
class DotnetPropertiesExtension(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _fileSystemService: FileSystemService)
    : AgentLifeCycleAdapter(), DotnetCliToolInfo {

    private var _subscriptionToken: Disposable
    private var _version: Version = jetbrains.buildServer.dotnet.Version.Empty

    init {
        _subscriptionToken = agentLifeCycleEventSources.beforeAgentConfigurationLoadedSource.subscribe {
            LOG.debug("Locating .NET CLI")
            try {
                val command = CommandLine(
                        TargetType.Tool,
                        File(_toolProvider.getPath(DotnetConstants.EXECUTABLE)),
                        File("."),
                        versionArgs,
                        emptyList())

                val agent = it.agent
                _commandLineExecutor.tryExecute(command)?.let {
                    _versionParser.tryParse(it.standardOutput)?.let {
                        _version = jetbrains.buildServer.dotnet.Version.parse(it)
                        val configuration = agent.configuration

                        configuration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, it)
                        LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_NAME}\": \"$it\"")

                        val dotnetPath = command.executableFile
                        configuration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, dotnetPath.absolutePath)
                        LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_PATH}\": \"${dotnetPath.absolutePath}\"")
                        LOG.info(".NET CLI $it found at \"${dotnetPath.absolutePath}\"")

                        LOG.debug("Locating .NET Core SDKs")

                        val sdks = _fileSystemService.list(File(dotnetPath.parentFile, "sdk"))
                                .filter { _fileSystemService.isDirectory(it) }
                                .map { Sdk(it, jetbrains.buildServer.dotnet.Version.parse(it.name)) }
                                .filter { it.version != jetbrains.buildServer.dotnet.Version.Empty }

                        for ((path, version) in enumerateSdk(sdks)) {
                            val paramName = "${DotnetConstants.CONFIG_SDK_NAME}$version${DotnetConstants.PATH_SUFFIX}"
                            val paramValue = path.absolutePath
                            configuration.addConfigurationParameter(paramName, paramValue)
                            LOG.debug("Add configuration parameter \"$paramName\": \"$paramValue\"")
                            LOG.info(".NET Core SDK $paramValue found at \"$paramValue\"")
                        }
                    }
                }

            } catch (e: ToolCannotBeFoundException) {
                LOG.info(".NET CLI not found")
                LOG.debug(e)
            }
        }
    }

    override val version: Version get() = _version

    companion object {
        private val LOG = Logger.getInstance(DotnetPropertiesExtension::class.java.name)

        internal fun enumerateSdk(versions: Sequence<Sdk>): Sequence<Sdk> = buildSequence {
            val groupedVersions = versions.groupBy { Version(*it.version.fullVersion.take(2).toIntArray()) }
            for ((version, sdks) in groupedVersions) {
                yield(Sdk(sdks.maxBy { it.version }!!.path, version))
                yieldAll(sdks)
            }
        }

        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }

    data class Sdk(val path: File, val version: Version)
}