package jetbrains.buildServer.agent

import java.io.OutputStream

interface Serializer<T> {
    fun serialize(obj: T, outputStream: OutputStream)
}