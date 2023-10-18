package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.EnvironmentBuilder
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.emptyDisposable

internal class TestCommandEnvironmentBuilder(
    private val _runnerScopedTestEnvironmentBuilder: BuildStepScopedTestEnvironmentBuilder,
) : EnvironmentBuilder {
    override fun build(context: DotnetCommandContext): Disposable {
        _runnerScopedTestEnvironmentBuilder.setupEnvironmentForTestReporting(context.verbosityLevel)
        return emptyDisposable()
    }
}
