package jetbrains.buildServer.dotnet.commands.dotnet

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.CommandType

/**
 * Provides parameters for dotnet clean id.
 */
class CleanCommandType : CommandType() {
    override val name: String
        get() = DotnetCommandType.Clean.id

    override val editPage: String
        get() = "editCleanParameters.jsp"

    override val viewPage: String
        get() = "viewCleanParameters.jsp"
}