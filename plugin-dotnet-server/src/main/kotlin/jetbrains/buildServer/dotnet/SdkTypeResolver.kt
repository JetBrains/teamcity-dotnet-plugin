package jetbrains.buildServer.dotnet

interface SdkTypeResolver {
    fun tryResolve(sdkVersion: Version): SdkType?
}