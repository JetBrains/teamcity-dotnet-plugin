package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory

/**
 * Provides parameters for dotnet pack command.
 */
class PackCommandType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.Pack.id

    override val editPage: String = "editPackParameters.jsp"

    override val viewPage: String = "viewPackParameters.jsp"
}