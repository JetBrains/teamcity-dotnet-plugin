package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

class MSBuildRequirementsProvider : DotnetCommandRequirementsProvider {
    override val commandType =  DotnetCommandType.MSBuild

    override fun getRequirements(parameters: Map<String, String>) = sequence {
        var shouldBeWindows = false
        var hasRequirement = false

        parameters[DotnetConstants.PARAM_MSBUILD_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.MSBuild) {
                    when (it.platform) {
                        ToolPlatform.Windows -> {
                            shouldBeWindows = true
                            hasRequirement = when (it.bitness) {
                                ToolBitness.X64 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x64_Path", null, RequirementType.EXISTS))
                                    true
                                }
                                ToolBitness.X86 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x86_Path", null, RequirementType.EXISTS))
                                    true
                                }
                                else -> {
                                    yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "MSBuildTools${it.version}\\.0_.+_Path", null, RequirementType.EXISTS))
                                    true
                                }
                            }
                        }
                        ToolPlatform.Mono -> {
                            yield(Requirement(MonoConstants.CONFIG_PATH, null, RequirementType.EXISTS))
                            hasRequirement = true
                        }
                        else -> { }
                    }
                }
            }
        }

        if (!hasRequirement) {
            yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS))
        }

        if (shouldBeWindows) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }
}

