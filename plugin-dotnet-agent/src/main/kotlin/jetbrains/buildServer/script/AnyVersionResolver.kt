package jetbrains.buildServer.script

import java.io.File

interface AnyVersionResolver {
    fun resolve(toolPath: File) : CsiTool
}