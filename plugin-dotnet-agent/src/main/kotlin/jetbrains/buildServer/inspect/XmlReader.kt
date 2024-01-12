

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
import java.io.InputStream

interface XmlReader {
    fun read(xmlStream: InputStream): Sequence<XmlElement>
}