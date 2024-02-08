package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory

/**
 * Provides parameters for dotnet clean command.
 */
class CleanCommandType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.Clean.id

    override val editPage: String = "editCleanParameters.jsp"

    override val viewPage: String = "viewCleanParameters.jsp"
}