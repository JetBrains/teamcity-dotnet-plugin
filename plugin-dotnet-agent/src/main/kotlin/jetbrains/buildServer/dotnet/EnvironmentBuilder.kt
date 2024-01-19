

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.emptyDisposable

interface EnvironmentBuilder {
    fun build(context: DotnetCommandContext): EnvironmentBuildResult
}

class EnvironmentBuildResult(
    val variables: Sequence<CommandLineEnvironmentVariable> = sequenceOf(),
    private val disposable: Disposable = emptyDisposable()
) : Disposable by disposable