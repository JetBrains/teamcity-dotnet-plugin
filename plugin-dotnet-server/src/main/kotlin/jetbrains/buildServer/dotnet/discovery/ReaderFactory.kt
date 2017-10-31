package jetbrains.buildServer.dotnet.discovery

import java.io.InputStream
import java.io.Reader

interface ReaderFactory {
    fun create(inputStream: InputStream): Reader
}