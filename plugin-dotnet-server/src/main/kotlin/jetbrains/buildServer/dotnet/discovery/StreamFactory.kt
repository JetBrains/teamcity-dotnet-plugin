package jetbrains.buildServer.dotnet.discovery

import java.io.InputStream

interface StreamFactory {
    fun tryCreate(path: String): InputStream?
}