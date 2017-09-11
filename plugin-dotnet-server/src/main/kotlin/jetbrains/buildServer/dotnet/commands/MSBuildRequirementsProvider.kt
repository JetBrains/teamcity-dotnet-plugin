package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

class MSBuildRequirementsProvider(private val _dotCoverInfoProvider: DotCoverInfoProvider) {
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    public fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement> = buildSequence {
        var shouldBeWindows = false;
        var hasRequirement = false;
        runParameters[DotnetConstants.PARAM_MSBUILD_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.MSBuild) {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (it.platform) {
                        ToolPlatform.Windows -> {
                            shouldBeWindows = true
                            when(it.bitness) {
                                ToolBitness.x64 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x64_Path", null, RequirementType.EXISTS))
                                    hasRequirement = true
                                }
                                ToolBitness.x86 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x86_Path", null, RequirementType.EXISTS))
                                    hasRequirement = true
                                }
                                else -> {
                                    yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "MSBuildTools${it.version}\\.0_.+_Path", null, RequirementType.EXISTS))
                                    hasRequirement = true
                                }
                            }
                        }
                    }
                }
            }
        }

        if(!hasRequirement) {
            yield(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
        }

        if (shouldBeWindows || _dotCoverInfoProvider.isCoverageEnabled(runParameters)) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }
}