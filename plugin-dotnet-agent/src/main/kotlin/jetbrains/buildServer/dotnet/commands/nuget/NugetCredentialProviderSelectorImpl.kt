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

package jetbrains.buildServer.dotnet.commands.nuget

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntimesProvider

class NugetCredentialProviderSelectorImpl(
    private val _parametersService: ParametersService,
    private val _runtimesProvider: DotnetRuntimesProvider,
    private val _virtualContext: VirtualContext
) : NugetCredentialProviderSelector {

    override fun trySelect(sdkVersion: Version): String? {
        val credentialProviderDisabled = _parametersService.tryGetParameter(ParameterType.Configuration, "teamcity.nuget.credentialprovider.disabled")?.trim()?.toBoolean() ?: false
        if (credentialProviderDisabled) {
            LOG.debug("Credentials plugin is disabled")
            return null
        }

        var sdkMajorVersion = sdkVersion.major
        if (sdkVersion == Version.Empty) {
            // for full .NET 4.6 bin\credential-plugin\net46\CredentialProvider.TeamCity.exe
            sdkMajorVersion = 4
            LOG.debug("Will use credentials plugin for .NET Framework")
        } else if (sdkVersion < Version.CredentialProviderVersion) {
            LOG.debug("Credentials plugin is not supported for SDK version $sdkVersion")
            return null
        }

        var credentialProviderPath = trySelectProviderBySdkMajorVersion(sdkMajorVersion)
        if (credentialProviderPath == null) {
            LOG.debug("Couldn't find credential plugin for SDK major version $sdkMajorVersion")

            if (sdkVersion != Version.Empty && !_virtualContext.isVirtual) {
                credentialProviderPath = trySelectProviderByAvailableRuntimes()
            }
        }

        if (credentialProviderPath != null) {
            credentialProviderPath = _virtualContext.resolvePath(credentialProviderPath)
            LOG.debug("Credentials provider found, using $credentialProviderPath")
        } else {
            LOG.debug("Credentials provider not found")
        }

        return credentialProviderPath
    }

    private fun trySelectProviderBySdkMajorVersion(sdkMajorVersion: Int): String? {
        LOG.debug("Selecting credentials plugin by SDK major version $sdkMajorVersion")
        return _parametersService.tryGetParameter(ParameterType.Configuration, "$CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER$sdkMajorVersion.0.0$CONFIG_SUFFIX_PATH")
    }

    private fun trySelectProviderByAvailableRuntimes(): String? {
        val runtimeVersions = _runtimesProvider.getRuntimes().map { it.version.major }.toSet()
        LOG.debug("Selecting credentials plugin by available runtimes: $runtimeVersions")

        val pluginVersionToNameMap = _parametersService.getParameterNames(ParameterType.Configuration)
            .filter { it.startsWith(CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER) }
            .filter { it.endsWith(CONFIG_SUFFIX_PATH) }
            .filter { _parametersService.tryGetParameter(ParameterType.Configuration, it) != null }
            .map { Version.parse(it.substring(CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER.length, it.length - CONFIG_SUFFIX_PATH.length)) to it }
            .filter { it.first != Version.Empty }
            .associate { it.first to it.second }

        var credentialProviderName = pluginVersionToNameMap
            .filter { runtimeVersions.contains(it.key.major) }
            .maxByOrNull { it.key }?.value

        if (credentialProviderName == null) {
            LOG.debug("Couldn't find pair of matching runtime and credentials plugin, trying to find a roll-forward pair")
            credentialProviderName = pluginVersionToNameMap
                .filter { runtimeVersions.any { runtimeVersion -> runtimeVersion > it.key.major } }
                .maxByOrNull { it.key }?.value
        }

        return credentialProviderName?.let { _parametersService.tryGetParameter(ParameterType.Configuration, it) }
    }

    companion object {
        private val LOG = Logger.getLogger(NugetCredentialProviderSelectorImpl::class.java)
    }
}