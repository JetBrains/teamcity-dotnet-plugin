package jetbrains.buildServer.script

import java.io.File

interface ToolResolver {
    fun resolve(): CsiTool
}