package jetbrains.buildServer.dotnet

import java.io.File

interface ToolResolver {
    val executableFile: File

    val isCommandRequired: Boolean
}