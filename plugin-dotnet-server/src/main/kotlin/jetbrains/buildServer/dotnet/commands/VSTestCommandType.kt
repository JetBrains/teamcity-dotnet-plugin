package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides parameters for dotnet VSTest command.
 */
class VSTestCommandType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory
) : CommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.VSTest.id

    override val editPage: String = "editVSTestParameters.jsp"

    override val viewPage: String = "viewVSTestParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
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