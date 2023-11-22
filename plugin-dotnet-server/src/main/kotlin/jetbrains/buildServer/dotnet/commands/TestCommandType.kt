

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.RequirementFactory

/**
 * Provides parameters for dotnet test command.
 */
class TestCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetCommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Test.id

    override val editPage: String = "editTestParameters.jsp"

    override val viewPage: String = "viewTestParameters.jsp"
}