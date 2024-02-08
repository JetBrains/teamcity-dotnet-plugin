package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory

/**
 * Provides parameters for dotnet restore command.
 */
class RestoreCommandType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory,
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.Restore.id

    override val editPage: String = "editRestoreParameters.jsp"

    override val viewPage: String = "viewRestoreParameters.jsp"
}