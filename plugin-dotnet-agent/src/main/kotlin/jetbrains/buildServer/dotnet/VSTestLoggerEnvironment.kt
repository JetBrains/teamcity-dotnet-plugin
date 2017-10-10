package jetbrains.buildServer.dotnet

import java.io.Closeable
import java.io.File

interface VSTestLoggerEnvironment {
    fun configure(targets: List<File>): Closeable
}