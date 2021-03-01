package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_SDK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.discovery.Framework
import jetbrains.buildServer.dotnet.discovery.SdkResolver
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier.EXISTS_QUALIFIER
import jetbrains.buildServer.requirements.RequirementType

class RequirementFactoryImpl(
        private val _sdkTypeResolver: SdkTypeResolver,
        private val _sdkResolver: SdkResolver)
    : RequirementFactory {
    override fun tryCreate(sdkVersion: String) =
        Version.tryParse(sdkVersion)?.let {
            version ->
            _sdkTypeResolver.tryResolve(version)?.let {
                createRequirement(
                    when(it) {
                        SdkType.Dotnet, SdkType.DotnetCore -> {
                            val versions = (sequenceOf(version) + _sdkResolver.getCompatibleVersions(it, version))
                                    .distinct()
                                    .toList()

                            when (versions.size) {
                                1 -> "$CONFIG_PREFIX_CORE_SDK$version$AnyVersion$CONFIG_SUFFIX_PATH"
                                else -> {
                                    val versionsStr = versions.joinToString("|")
                                    "$CONFIG_PREFIX_CORE_SDK($versionsStr)$AnyVersion$CONFIG_SUFFIX_PATH"
                                }
                            }
                        }
                        SdkType.FullDotnetTargetingPack -> "$CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK$version$CONFIG_SUFFIX_PATH"
                        SdkType.DotnetFramework -> "$CONFIG_PREFIX_DOTNET_FAMEWORK$version[\\.\\d]*_x[\\d]{2}"
                    })
            }
        }

    companion object {
        private const val AnyVersion = "[\\.\\d]*"
        private fun createRequirement(regex: String) =
                Requirement("$EXISTS_QUALIFIER($regex)", null, RequirementType.EXISTS)
    }
}