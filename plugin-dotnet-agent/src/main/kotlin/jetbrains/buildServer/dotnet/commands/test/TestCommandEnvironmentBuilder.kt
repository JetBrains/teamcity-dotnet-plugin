package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.EnvironmentBuilder
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.emptyDisposable

internal class TestCommandEnvironmentBuilder(
    private val _runnerScopedTestEnvironmentBuilder: BuildStepScopedTestEnvironmentBuilder,
) : EnvironmentBuilder {
    override fun build(context: DotnetBuildContext): Disposable {
        _runnerScopedTestEnvironmentBuilder.setupEnvironmentForTestReporting()
        return emptyDisposable()
    }
}
