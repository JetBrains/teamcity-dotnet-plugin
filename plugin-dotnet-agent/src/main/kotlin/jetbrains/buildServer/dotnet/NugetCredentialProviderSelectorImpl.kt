package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_SDK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH

class NugetCredentialProviderSelectorImpl(
        private val _parametersService: ParametersService,
        private val _runtimesProvider: DotnetRuntimesProvider,
        private val _virtualContext: VirtualContext)
    : NugetCredentialProviderSelector {

    override fun trySelect(sdkVersion: Version): String? {
        val credentialproviderDisabled = _parametersService.tryGetParameter(ParameterType.Configuration, "teamcity.nuget.credentialprovider.disabled")?.trim()?.toBoolean() ?: false
        if (!credentialproviderDisabled) {
            var majorVersion = sdkVersion.major
            if (majorVersion == 0) {
                // for full .NET 4.6 bin\credential-plugin\net46\CredentialProvider.TeamCity.exe
                majorVersion = 4
                LOG.debug("Use credentials plugin for full .NET")
            }

            if (sdkVersion == Version.Empty || sdkVersion >= Version.CredentialProviderVersion) {
                var credentialproviderPath = _parametersService.tryGetParameter(ParameterType.Configuration, "DotNetCredentialProvider$majorVersion.0.0_Path")

                if (credentialproviderPath == null && sdkVersion != Version.Empty && !_virtualContext.isVirtual) {
                    LOG.debug("Cannot find .NET SDK version for credentials plugin $majorVersion")
                    var runtimeVersions = _runtimesProvider.getRuntimes().map { it.version.major }.toSet()
                    credentialproviderPath =
                            _parametersService
                                    .getParameterNames(ParameterType.Configuration)
                                    .filter { it.startsWith(CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER) }
                                    .filter { it.endsWith(CONFIG_SUFFIX_PATH) }
                                    .map { it to Version.parse(it.substring(CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER.length, it.length - CONFIG_SUFFIX_PATH.length)) }
                                    .filter { it.second != Version.Empty }
                                    .filter { runtimeVersions.contains(it.second.major) }
                                    .maxByOrNull { it.second }
                                    ?.first
                            ?.let {  _parametersService.tryGetParameter(ParameterType.Configuration, it) }
                }

                if (credentialproviderPath != null) {
                    return _virtualContext.resolvePath(credentialproviderPath)
                }
            } else {
                LOG.debug("Credentials plugin is not supported")
            }
        } else {
            LOG.debug("Credentials plugin is disabled")
        }

        return null
    }

    companion object {
        private val LOG = Logger.getLogger(EnvironmentVariablesImpl::class.java)
    }
}