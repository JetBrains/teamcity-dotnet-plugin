package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.requirements.SDKBasedRequirementFactory

/**
 * Provides parameters for dotnet test command.
 */
class TestCommandType(
    sdkBasedRequirementFactory: SDKBasedRequirementFactory
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.Test.id

    override val editPage: String = "editTestParameters.jsp"

    override val viewPage: String = "viewTestParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))
        yieldAll(TestRetryCountValidator.validate(properties))
    }
}