package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.CommandType

/**
 * Provides parameters for dotnet clean command.
 */
class CleanCommandType : DotnetType() {
    override val name: String
        get() = DotnetCommandType.Clean.id

    override val editPage: String
        get() = "editCleanParameters.jsp"

    override val viewPage: String
        get() = "viewCleanParameters.jsp"
}