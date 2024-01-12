

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
import java.io.OutputStream

interface XmlWriter {
    fun write(rootElement: XmlElement, xmlStream: OutputStream)
}