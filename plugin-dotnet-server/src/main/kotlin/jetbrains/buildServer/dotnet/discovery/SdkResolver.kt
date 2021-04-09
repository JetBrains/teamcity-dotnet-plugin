package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.Version

interface SdkResolver {
    fun resolveSdkVersions(framework: Framework, propeties: Collection<Property>): Sequence<SdkVersion>

    fun getCompatibleVersions(version: Version): Sequence<SdkVersion>
}