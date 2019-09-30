package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ToolPath

interface ToolResolver {
    val paltform: ToolPlatform

    val executable: ToolPath

    val isCommandRequired: Boolean
}