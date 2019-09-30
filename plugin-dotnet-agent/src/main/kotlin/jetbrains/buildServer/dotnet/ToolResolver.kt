package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Path
import java.io.File

interface ToolResolver {
    val paltform: ToolPlatform

    val executableFile: Path

    val isCommandRequired: Boolean
}