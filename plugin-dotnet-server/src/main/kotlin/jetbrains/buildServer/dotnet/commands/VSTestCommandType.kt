@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet VSTest command.
 */
class VSTestCommandType : CommandType() {
    override val name: String = DotnetCommandType.VSTest.id

    override val editPage: String = "editVSTestParameters.jsp"

    override val viewPage: String = "viewVSTestParameters.jsp"

    override fun getRequirements(parameters: Map<String, String>) = buildSequence {
        if (isDocker(parameters)) return@buildSequence

        var shouldBeWindows = false
        var hasRequirement = false
        parameters[DotnetConstants.PARAM_VSTEST_VERSION]?.let {
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

    override fun validateProperties(properties: Map<String, String>) = buildSequence {
        yieldAll(super.validateProperties(properties))
        DotnetConstants.PARAM_PATHS.let {
            if (properties[it].isNullOrBlank()) {
                yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        when (properties[DotnetConstants.PARAM_TEST_FILTER]) {
            "name" -> {
                DotnetConstants.PARAM_TEST_NAMES.let {
                    if (properties[it].isNullOrBlank()) {
                        yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
                    }
                }
            }
            "filter" -> {
                DotnetConstants.PARAM_TEST_CASE_FILTER.let {
                    if (properties[it].isNullOrBlank()) {
                        yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
                    }
                }
            }
        }
    }
}