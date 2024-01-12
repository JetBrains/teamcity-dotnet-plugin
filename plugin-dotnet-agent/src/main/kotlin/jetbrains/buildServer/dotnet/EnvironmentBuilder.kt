

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.rx.Disposable

interface EnvironmentBuilder {
    fun build(context: DotnetCommandContext): Disposable
}