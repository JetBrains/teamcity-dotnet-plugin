package jetbrains.buildServer.dotCover.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.RequirementFactory
import jetbrains.buildServer.dotnet.commands.DotnetCLICommandType

class CoverCommandType(
    private val _requirementFactory: RequirementFactory
)
    : DotnetCLICommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Build.id

    override val editPage: String = "editBuildParameters.jsp"

    override val viewPage: String = "viewBuildParameters.jsp"
}