package jetbrains.buildServer.dotnet

import java.io.File

interface ToolResolver {
    val paltform: ToolPlatform

    val executableFile: File

    val isCommandRequired: Boolean
}