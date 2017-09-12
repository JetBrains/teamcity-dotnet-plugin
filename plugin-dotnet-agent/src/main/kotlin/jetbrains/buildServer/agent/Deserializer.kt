package jetbrains.buildServer.agent

import java.io.InputStream

interface Deserializer<T> {
    fun deserialize(inputStream: InputStream): T;
}