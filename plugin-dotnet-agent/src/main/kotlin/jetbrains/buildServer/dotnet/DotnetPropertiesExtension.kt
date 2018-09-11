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
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
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
        private val _dotnetCliToolInfo: DotnetCliToolInfo,
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService)
    : AgentLifeCycleAdapter() {

    private var _subscriptionToken: Disposable

    init {
        _subscriptionToken = agentLifeCycleEventSources.beforeAgentConfigurationLoadedSource.subscribe {
            LOG.debug("Locating .NET CLI")
            try {
                val configuration = it.agent.configuration

                // Detect .NET CLI path
                val dotnetPath = File(_toolProvider.getPath(DotnetConstants.EXECUTABLE))
                configuration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, dotnetPath.absolutePath)
                LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_PATH}\": \"${dotnetPath.absolutePath}\"")
                LOG.info(".NET CLI $it found at \"${dotnetPath.absolutePath}\"")

                // Detect .NET CLi version
                val defaultSdkVersion = _dotnetCliToolInfo.getVersion(dotnetPath, _pathsService.getPath(PathType.Work)).toString()
                configuration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, defaultSdkVersion)
                LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_NAME}\": \"$defaultSdkVersion\"")

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

            } catch (e: ToolCannotBeFoundException) {
                LOG.info(".NET CLI not found")
                LOG.debug(e)
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetPropertiesExtension::class.java.name)

        internal fun enumerateSdk(versions: Sequence<Sdk>): Sequence<Sdk> = buildSequence {
            val groupedVersions = versions.groupBy { Version(*it.version.fullVersion.take(2).toIntArray()) }
            for ((version, sdks) in groupedVersions) {
                yield(Sdk(sdks.maxBy { it.version }!!.path, version))
                yieldAll(sdks)
            }
        }
    }

    data class Sdk(val path: File, val version: Version)
}