package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_SDK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier.EXISTS_QUALIFIER
import jetbrains.buildServer.requirements.RequirementType

class RequirementFactoryImpl: RequirementFactory {
    override fun tryCreate(sdkVersion: String) =
        Version.tryParse(sdkVersion)?.let {
            when {
                it `in` DotnetCoreVersions -> Requirement("$EXISTS_QUALIFIER$CONFIG_PREFIX_CORE_SDK$it[\\.\\d]*$CONFIG_SUFFIX_PATH", null, RequirementType.EXISTS)
                it `in` DotnetFullVersions -> Requirement("$EXISTS_QUALIFIER$CONFIG_PREFIX_DOTNET_FAMEWORK$it[\\.\\d]*_x[\\d]{2}", null, RequirementType.EXISTS)
                else -> null
            }
        }

    companion object {
        private val DotnetCoreVersions = combineOf(
                Version(1).including() to Version(3, 5).excluding(),
                Version(5).including() to Version(Int.MAX_VALUE).including()
        )

        private val DotnetFullVersions = combineOf(
                Version(3, 5).including() to Version(5).excluding()
        )
    }
}