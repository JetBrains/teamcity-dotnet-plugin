package jetbrains.buildServer.dotnet

import java.io.Closeable

interface EnvironmentBuilder {
    fun build(context: DotnetBuildContext): Closeable
}