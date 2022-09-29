package jetbrains.buildServer.dotnet.logging

import jetbrains.buildServer.dotnet.ToolType
import java.io.File

interface LoggerResolver {
    fun resolve(toolType: ToolType): File
}