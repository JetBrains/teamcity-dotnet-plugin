package jetbrains.buildServer.dotnet

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

class SdkTypeResolverImpl : SdkTypeResolver {
    override fun tryResolve(sdkVersion: Version): SdkType? =
            when {
                sdkVersion `in` DotnetVersions -> SdkType.Dotnet
                sdkVersion `in` DotnetCoreVersions -> SdkType.DotnetCore
                sdkVersion `in` DotnetFullVersions -> SdkType.FullDotnet
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