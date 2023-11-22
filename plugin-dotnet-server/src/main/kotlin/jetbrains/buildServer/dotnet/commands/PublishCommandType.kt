

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.RequirementFactory

/**
 * Provides parameters for dotnet publish command.
 */
class PublishCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetCommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Publish.id

    override val editPage: String = "editPublishParameters.jsp"

    override val viewPage: String = "viewPublishParameters.jsp"
}