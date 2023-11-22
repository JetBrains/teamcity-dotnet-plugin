

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.RequirementFactory

/**
 * Provides parameters for dotnet restore command.
 */
class RestoreCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetCommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Restore.id

    override val editPage: String = "editRestoreParameters.jsp"

    override val viewPage: String = "viewRestoreParameters.jsp"
}