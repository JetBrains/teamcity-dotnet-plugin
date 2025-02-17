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
            version >= CrossPlatformVersion && platform == InspectionToolPlatform.CrossPlatform -> yield(DotnetCoreRuntime31Requirement)
            version >= RequiresNet461Version && platform == InspectionToolPlatform.WindowsX86 -> yield(DotnetFramework461X86Requirement)
            version >= RequiresNet461Version && platform == InspectionToolPlatform.WindowsX64 -> yield(DotnetFramework461X64Requirement)
            platform == InspectionToolPlatform.WindowsX86 -> yield(DotnetFrameworkX86Requirement)
            platform == InspectionToolPlatform.WindowsX64 -> yield(DotnetFrameworkX64Requirement)
            else -> emptySequence<Requirement>()
        }
    }

    companion object {
        internal val DotnetFrameworkX64Regexp = "${CONFIG_PREFIX_DOTNET_FRAMEWORK}[\\d\\.]+_x64${CONFIG_SUFFIX_PATH}"
        internal val DotnetFrameworkX86Regexp = "${CONFIG_PREFIX_DOTNET_FRAMEWORK}[\\d\\.]+_x86${CONFIG_SUFFIX_PATH}"
        internal val DotnetFramework461X64Regexp = "(${CONFIG_PREFIX_DOTNET_FRAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x64${CONFIG_SUFFIX_PATH})"
        internal val DotnetFramework461X86Regexp = "(${CONFIG_PREFIX_DOTNET_FRAMEWORK}4\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_x86${CONFIG_SUFFIX_PATH})"
        internal val DotnetCoreRuntime31PathRegexp = "^${CONFIG_PREFIX_CORE_RUNTIME}(?:(?:3\\.[1-9]\\d*)|(?:(?:[5-9]|[1-9]\\d+)\\.\\d+))[a-zA-Z\\d\\.\\-]*${CONFIG_SUFFIX_PATH}\$"

        internal val DotnetFrameworkX64Requirement = createExistsRequirement(DotnetFrameworkX64Regexp)
        internal val DotnetFrameworkX86Requirement = createExistsRequirement(DotnetFrameworkX86Regexp)
        internal val DotnetFramework461X64Requirement = createExistsRequirement(DotnetFramework461X64Regexp)
        internal val DotnetFramework461X86Requirement = createExistsRequirement(DotnetFramework461X86Regexp)
        internal val DotnetCoreRuntime31Requirement = createExistsRequirement(DotnetCoreRuntime31PathRegexp)

        private val RequiresNet461Version = Version(2018, 2)
        internal val CrossPlatformVersion = Version(2020, 2, 1)
        internal val LastVersionWithDupFinder = Version(2021, 2, 3)

        private fun createExistsRequirement(regex: String) = Requirement(RequirementQualifier.EXISTS_QUALIFIER + regex, null, RequirementType.EXISTS)
    }
}