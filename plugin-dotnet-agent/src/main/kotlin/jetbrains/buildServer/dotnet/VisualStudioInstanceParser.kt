package jetbrains.buildServer.dotnet

import com.google.gson.JsonIOException
import java.io.InputStream

interface VisualStudioInstanceParser {
    @Throws(Exception::class)
    fun tryParse(stream: InputStream): VisualStudioInstance?
}