package jetbrains.buildServer.script

import java.io.File

interface ToolVersionResolver {
    fun resolve(toolPath: File) : CsiTool
}