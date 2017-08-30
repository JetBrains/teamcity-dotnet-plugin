package jetbrains.buildServer.runners

import java.io.OutputStream

interface Serializer<T> {
    fun serialize(obj: T, outputStream: OutputStream)
}