package jetbrains.buildServer.agent

import java.io.InputStream

interface Deserializer<out T> {
    fun deserialize(inputStream: InputStream): T
}