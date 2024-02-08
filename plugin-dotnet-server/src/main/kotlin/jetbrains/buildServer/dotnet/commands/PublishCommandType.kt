package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory

/**
 * Provides parameters for dotnet publish command.
 */
class PublishCommandType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.Publish.id

    override val editPage: String = "editPublishParameters.jsp"

    override val viewPage: String = "viewPublishParameters.jsp"
}