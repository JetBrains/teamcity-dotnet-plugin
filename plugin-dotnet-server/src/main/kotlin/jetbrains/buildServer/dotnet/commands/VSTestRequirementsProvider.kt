package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

class VSTestRequirementsProvider(private val _dotCoverInfoProvider: DotCoverInfoProvider) {
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    public fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement> = buildSequence {
        var shouldBeWindows = false;
        var hasRequirement = false;
        runParameters[DotnetConstants.PARAM_VSTEST_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.VSTest) {
                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (it.platform) {
                        ToolPlatform.Windows -> {
                            yield(Requirement("teamcity.dotnet.vstest.${it.version}.0", null, RequirementType.EXISTS))
                            shouldBeWindows = true
                            hasRequirement = true
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