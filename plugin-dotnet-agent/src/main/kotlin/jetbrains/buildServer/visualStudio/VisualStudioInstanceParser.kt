package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.visualStudio.VisualStudioInstance
import java.io.InputStream

interface VisualStudioInstanceParser {
    @Throws(Exception::class)
    fun tryParse(stream: InputStream): VisualStudioInstance?
}