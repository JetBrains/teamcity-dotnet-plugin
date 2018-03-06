package jetbrains.buildServer.dotnet

import java.io.Closeable
import java.io.File

interface EnvironmentBuilder {
    fun build(command: DotnetCommand): Closeable
}