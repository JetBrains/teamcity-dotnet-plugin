package jetbrains.buildServer.dotnet

import java.io.File

interface LoggerResolver {
    fun resolve(toolType: ToolType): File
}