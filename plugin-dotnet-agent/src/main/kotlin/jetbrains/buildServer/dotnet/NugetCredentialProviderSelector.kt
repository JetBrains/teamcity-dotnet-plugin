package jetbrains.buildServer.dotnet

interface NugetCredentialProviderSelector {
    fun trySelect(sdkVersion: Version): String?
}