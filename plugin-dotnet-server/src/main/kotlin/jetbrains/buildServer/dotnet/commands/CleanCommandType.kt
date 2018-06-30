package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType

/**
 * Provides parameters for dotnet clean command.
 */
class CleanCommandType : DotnetType() {
    override val name: String = DotnetCommandType.Clean.id

    override val editPage: String = "editCleanParameters.jsp"

    override val viewPage: String = "viewCleanParameters.jsp"
}