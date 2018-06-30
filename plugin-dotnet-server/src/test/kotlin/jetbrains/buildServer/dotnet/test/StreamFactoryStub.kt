package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.discovery.StreamFactory
import java.io.InputStream

class StreamFactoryStub : StreamFactory {
    private val _streams: MutableMap<String, InputStream> = mutableMapOf()

    fun add(path: String, inputStream: InputStream): StreamFactoryStub {
        _streams[path] = inputStream
        return this
    }

    override fun tryCreate(path: String): InputStream? {
        return _streams[path]
    }
}