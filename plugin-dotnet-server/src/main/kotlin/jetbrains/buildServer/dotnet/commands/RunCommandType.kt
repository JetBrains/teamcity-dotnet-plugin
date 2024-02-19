package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.requirements.SDKBasedRequirementFactory

/**
 * Provides parameters for dotnet run command.
 */
class RunCommandType(
    sdkBasedRequirementFactory: SDKBasedRequirementFactory
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.Run.id

    override val editPage: String = "editRunParameters.jsp"

    override val viewPage: String = "viewRunParameters.jsp"
}