package jetbrains.buildServer.runners

import org.w3c.dom.Document
import java.io.InputStream
import java.io.OutputStream

interface Deserializer<T> {
    fun deserialize(inputStream: InputStream): T;
}