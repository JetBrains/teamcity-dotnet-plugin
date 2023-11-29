

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.RequirementFactory

/**
 * Provides parameters for dotnet clean command.
 */
class CleanCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetCLICommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Clean.id

    override val editPage: String = "editCleanParameters.jsp"

    override val viewPage: String = "viewCleanParameters.jsp"
}