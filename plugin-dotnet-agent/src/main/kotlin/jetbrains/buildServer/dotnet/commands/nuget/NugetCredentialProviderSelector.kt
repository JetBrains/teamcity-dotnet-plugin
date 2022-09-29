package jetbrains.buildServer.dotnet.commands.nuget

import jetbrains.buildServer.agent.Version

interface NugetCredentialProviderSelector {
    fun trySelect(sdkVersion: Version): String?
}