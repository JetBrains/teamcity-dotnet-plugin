package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

class RequirementsResolverImpl : RequirementsResolver {
    override fun resolve(version: Version, platform: InspectionToolPlatform) = sequence {
        when {
            version >= CrossPlatformVersion && platform == InspectionToolPlatform.CrossPlatform -> yield(DotnetCore3)
            version >= RequiresNet461Version && platform == InspectionToolPlatform.WindowsX86 -> yield(FullDotnet461X86)
            version >= RequiresNet461Version && platform == InspectionToolPlatform.WindowsX64 -> yield(FullDotnet461X64)
            platform == InspectionToolPlatform.WindowsX86 -> yield(MinimalRequirementX86)
            platform == InspectionToolPlatform.WindowsX64 -> yield(MinimalRequirementX64)
            else -> emptySequence<Requirement>()
        }
    }

    companion object {
        private val MinimalRequirementX64 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FRAMEWORK}[\\d\\.]+_x64${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)
        private val MinimalRequirementX86 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${CONFIG_PREFIX_DOTNET_FRAMEWORK}[\\d\\.]+_x86${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)
        private val FullDotnet461X64 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FRAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x64${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS)
        private val FullDotnet461X86 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(${CONFIG_PREFIX_DOTNET_FRAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x86${CONFIG_SUFFIX_PATH})", null, RequirementType.EXISTS)
        private val DotnetCore3 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${CONFIG_PREFIX_CORE_RUNTIME}3\\.[\\d\\.]+${CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)

        private val RequiresNet461Version = Version(2018, 2)
        internal val CrossPlatformVersion = Version(2020, 2, 1)
        internal val LastVersionWithDupFinder = Version(2021, 2, 3)
    }
}