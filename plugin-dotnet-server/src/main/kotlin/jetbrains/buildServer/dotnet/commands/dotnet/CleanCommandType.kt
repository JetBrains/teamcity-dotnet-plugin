package jetbrains.buildServer.dotnet.commands.dotnet

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.CommandType

/**
 * Provides parameters for dotnet clean command.
 */
class CleanCommandType : CommandType() {
    override val name: String
        get() = DotnetCommand.Clean.command

    override val editPage: String
        get() = "editCleanParameters.jsp"

    override val viewPage: String
        get() = "viewCleanParameters.jsp"
}