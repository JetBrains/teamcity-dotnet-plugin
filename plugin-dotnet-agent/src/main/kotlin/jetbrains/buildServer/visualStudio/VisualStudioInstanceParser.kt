package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.runner.ToolInstance
import java.io.InputStream

interface VisualStudioInstanceParser {
    @Throws(Exception::class)
    fun tryParse(stream: InputStream): ToolInstance?
}