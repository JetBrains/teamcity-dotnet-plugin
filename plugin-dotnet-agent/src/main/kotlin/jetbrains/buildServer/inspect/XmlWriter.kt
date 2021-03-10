package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import java.io.InputStream
import java.io.OutputStream

interface XmlWriter {
    fun write(rootElement: E, xmlStream: OutputStream)
}