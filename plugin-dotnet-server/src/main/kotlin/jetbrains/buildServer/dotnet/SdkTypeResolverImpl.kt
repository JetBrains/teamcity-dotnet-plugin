package jetbrains.buildServer.dotnet

import jetbrains.buildServer.excluding
import jetbrains.buildServer.including
import jetbrains.buildServer.to

class SdkTypeResolverImpl : SdkTypeResolver {
    override fun tryResolve(sdkVersion: Version): SdkType? =
            when {
                sdkVersion.versions.size == 1 && sdkVersion == Version(4) -> SdkType.DotnetFramework
                sdkVersion `in` DotnetVersions -> SdkType.Dotnet
                sdkVersion `in` DotnetCoreVersions -> SdkType.DotnetCore
                sdkVersion `in` DotnetFullVersions -> SdkType.FullDotnetTargetingPack
                else -> null
            }

    companion object {
        private val DotnetVersions =
                Version(5).including() to Version(Int.MAX_VALUE).including()

        private val DotnetCoreVersions =
                Version(1).including() to Version(3, 5).excluding()

        private val DotnetFullVersions =
                Version(3, 5).including() to Version(5).excluding()
    }
}