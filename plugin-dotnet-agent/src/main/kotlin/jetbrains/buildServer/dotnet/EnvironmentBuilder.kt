package jetbrains.buildServer.dotnet

import jetbrains.buildServer.rx.Disposable

interface EnvironmentBuilder {
    fun build(context: DotnetBuildContext): Disposable
}