

package jetbrains.buildServer

import java.io.InputStream

interface Deserializer<out T> {
    fun deserialize(inputStream: InputStream): T
}