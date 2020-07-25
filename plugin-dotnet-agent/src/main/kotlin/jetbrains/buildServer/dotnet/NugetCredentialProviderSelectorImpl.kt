package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import org.apache.log4j.Logger

class NugetCredentialProviderSelectorImpl(
        private val _parametersService: ParametersService,
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

                    val minSdkVersion = _parametersService.getParameterNames(ParameterType.Configuration)
                            .filter { it.startsWith("DotNetCoreSDK") }
                            .map { Version.parse(it.replace("DotNetCoreSDK", "").replace("_Path", "")) }
                            .filter { it != Version.Empty }
                            .map { it.major }
                            .ifEmpty { sequenceOf(1) }
                            .min() ?: 1

                    LOG.debug("Minimal .NET SDK version for credentials plugin is $minSdkVersion")
                    credentialproviderPath = _parametersService.tryGetParameter(ParameterType.Configuration, "DotNetCredentialProvider$minSdkVersion.0.0_Path")
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