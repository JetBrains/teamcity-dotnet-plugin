package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

class MSBuildRequirementsProvider(private val _dotCoverInfoProvider: DotCoverInfoProvider) {
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    public fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement> = buildSequence {
        var shouldBeWindows = false;
        runParameters[DotnetConstants.PARAM_MSBUILD_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.MSBuild) {
                    when (it.platform) {
                        ToolPlatform.Windows -> {
                            shouldBeWindows = true
                            when(it.bitness) {
                                ToolBitness.x64 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x64_Path", null, RequirementType.EXISTS))
                                }
                                ToolBitness.x86 -> {
                                    yield(Requirement("MSBuildTools${it.version}.0_x86_Path", null, RequirementType.EXISTS))
                                }
                                else -> {
                                    yield(Requirement("MSBuildTools${it.version}\\.0_.+_Path", null, RequirementType.MATCHES))
                                }
                            }
                        }
                        ToolPlatform.Any -> {
                            yield(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
                        }
                    }
                }
            }
        }

        if (shouldBeWindows || _dotCoverInfoProvider.isCoverageEnabled(runParameters)) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }
}