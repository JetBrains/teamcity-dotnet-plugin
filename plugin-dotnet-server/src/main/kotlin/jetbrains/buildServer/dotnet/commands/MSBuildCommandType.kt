package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.requirements.SDKBasedRequirementFactory

/**
 * Provides parameters for dotnet MSBuild command.
 */
class MSBuildCommandType(
    sdkBasedRequirementFactory: SDKBasedRequirementFactory
) : CommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.MSBuild.id

    override val editPage: String = "editMSBuildParameters.jsp"

    override val viewPage: String = "viewMSBuildParameters.jsp"
}