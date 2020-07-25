package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version

interface NugetCredentialProviderSelector {
    fun trySelect(sdkVersion: Version): String?
}