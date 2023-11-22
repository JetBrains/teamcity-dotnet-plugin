

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.RequirementFactory

/**
 * Provides parameters for dotnet build command.
 */
class BuildCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetCommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Build.id

    override val editPage: String = "editBuildParameters.jsp"

    override val viewPage: String = "viewBuildParameters.jsp"
}