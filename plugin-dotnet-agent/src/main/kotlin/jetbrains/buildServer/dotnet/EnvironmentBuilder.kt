package jetbrains.buildServer.dotnet

import jetbrains.buildServer.rx.Disposable
import java.io.Closeable

interface EnvironmentBuilder {
    fun build(context: DotnetBuildContext): Disposable
}