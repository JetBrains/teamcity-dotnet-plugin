package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet VSTest command.
 */
class VSTestCommandType : CommandType() {
    override val name: String
        get() = DotnetCommandType.VSTest.id

    override val editPage: String
        get() = "editVSTestParameters.jsp"

    override val viewPage: String
        get() = "viewVSTestParameters.jsp"

    override fun getRequirements(runParameters: Map<String, String>) = buildSequence {
        var shouldBeWindows = false
        var hasRequirement = false
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

        if (!hasRequirement) {
            yield(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
        }

        if (shouldBeWindows) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }
}