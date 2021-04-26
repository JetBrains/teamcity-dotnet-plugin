package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import java.io.InputStream

interface XmlReader {
    fun read(xmlStream: InputStream): Sequence<E>
}