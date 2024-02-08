package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.DotnetCommandType

interface DotnetCommandRequirementsProvider : RequirementsProvider {
    val commandType: DotnetCommandType
}