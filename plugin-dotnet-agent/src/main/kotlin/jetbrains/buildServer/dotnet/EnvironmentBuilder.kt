package jetbrains.buildServer.dotnet

import java.io.Closeable

interface EnvironmentBuilder {
    fun build(command: DotnetCommand): Closeable
}