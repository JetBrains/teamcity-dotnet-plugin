/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import org.apache.log4j.Logger
import java.io.File

/**`
 * Provides a list of available .NET CLI parameters.
 */
class DotnetPropertiesExtension(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        private val _toolProvider: ToolProvider,
        private val _dotnetVersionProvider: DotnetVersionProvider,
        private val _dotnetSdksProvider: DotnetSdksProvider,
        private val _pathsService: PathsService)
    : AgentLifeCycleAdapter() {

    private var _subscriptionToken: Disposable

    init {
        _subscriptionToken = agentLifeCycleEventSources.beforeAgentConfigurationLoadedSource.subscribe { event ->
            LOG.debug("Locating .NET CLI")
            try {
                val configuration = event.agent.configuration

                // Detect .NET CLI path
                val dotnetPath = File(_toolProvider.getPath(DotnetConstants.EXECUTABLE))
                configuration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, dotnetPath.canonicalPath)
                LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_PATH}\": \"${dotnetPath.canonicalPath}\"")
                LOG.info(".NET CLI $event found at \"${dotnetPath.absolutePath}\"")

                // Detect .NET CLI version
                val sdkVersion = _dotnetVersionProvider.getVersion(Path(dotnetPath.path), Path(_pathsService.getPath(PathType.Work).path))
                configuration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, sdkVersion.toString())
                LOG.debug("Add configuration parameter \"${DotnetConstants.CONFIG_NAME}\": \"${sdkVersion}\"")

                LOG.debug("Locating .NET Core SDKs")
                val sdks = _dotnetSdksProvider.getSdks(dotnetPath).toList()
                for ((version, path) in enumerateSdk(sdks)) {
                    val paramName = "${DotnetConstants.CONFIG_SDK_NAME}$version${DotnetConstants.PATH_SUFFIX}"
                    configuration.addConfigurationParameter(paramName, path)
                    LOG.debug("Add configuration parameter \"$paramName\": \"$path\"")
                    LOG.info(".NET Core SDK $version found at \"$path\"")
                }

            } catch (e: ToolCannotBeFoundException) {
                LOG.info(".NET CLI not found")
                LOG.debug(e)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetPropertiesExtension::class.java)

        internal fun enumerateSdk(versions: List<DotnetSdk>): Sequence<Pair<String, String>> = sequence {
            val groupedVersions = versions.filter { it.version != Version.Empty }.groupBy { Version(it.version.major, it.version.minor) }
            for ((version, sdks) in groupedVersions) {
                yield("${version.major}.${version.minor}" to sdks.maxBy { it.version }!!.path.absolutePath)
                yieldAll(sdks.map { it.version.toString() to it.path.absolutePath })
            }
        }
    }
}