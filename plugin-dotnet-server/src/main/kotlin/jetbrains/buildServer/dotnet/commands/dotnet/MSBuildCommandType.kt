package jetbrains.buildServer.dotnet.commands.dotnet

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.CommandType

/**
 * Provides parameters for dotnet build id.
 */
class MSBuildCommandType : CommandType() {
    override val name: String
        get() = DotnetCommandType.MSBuild.id

    override val editPage: String
        get() = "editMSBuildParameters.jsp"

    override val viewPage: String
        get() = "viewMSBuildParameters.jsp"
}