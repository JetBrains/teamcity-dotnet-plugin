package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.SdkType
import jetbrains.buildServer.dotnet.Version

data class SdkVersion(val version: Version, val sdkType: SdkType, val versionType: SdkVersionType) {
    override fun toString(): String {
        return "${if(versionType == SdkVersionType.Default) "*" else "" }${sdkType.shortDescription}$version"
    }
}