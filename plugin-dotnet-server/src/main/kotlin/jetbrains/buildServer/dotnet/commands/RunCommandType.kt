package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory

/**
 * Provides parameters for dotnet run command.
 */
class RunCommandType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.Run.id

    override val editPage: String = "editRunParameters.jsp"

    override val viewPage: String = "viewRunParameters.jsp"
}