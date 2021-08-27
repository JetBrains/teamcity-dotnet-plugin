package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.EventObserver
import jetbrains.buildServer.agent.EventSources
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_DOCKER_IMAGE
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe

class NugetEnvironmentImpl(
        private val _buildStepContext: BuildStepContext,
        private val _loggerService: LoggerService)
    : NugetEnvironment, EventObserver {

    override val allowInternalCaches: Boolean get() =
        if (_buildStepContext.isAvailable) _sameRunnersWithDockerWrapper.any() else false

    override fun subscribe(sources: EventSources): Disposable =
        sources.stepStartedSource.subscribe {
            if (allowInternalCaches) {
                if (RestoringRunners.contains(_buildStepContext.runnerContext.runType)) {
                    _loggerService.writeWarning("The default path to the NuGet global cache was overridden by a ${DotnetConstants.RUNNER_DISPLAY_NAME} build step run inside a Docker container. To prevent any issues with restoring NuGet packages in the current build step, use the related command of the universal ${DotnetConstants.RUNNER_DISPLAY_NAME} runner instead of the current runner.")
                }
            }
        }

    private val _incompatibleRunners: List<BuildRunnerSettings> get() =
        _buildStepContext.runnerContext.build.buildRunners
                .filter { RestoringRunners.contains(it.runType) }

    private val _sameRunnersWithDockerWrapper: List<BuildRunnerSettings> get() =
        _buildStepContext.runnerContext.build.buildRunners
                .filter { DotnetConstants.RUNNER_TYPE.equals(it.runType, true) }
                .filter { !it.runnerParameters[PARAM_DOCKER_IMAGE].isNullOrBlank() }

    companion object {
        internal val RestoringRunners = setOf("MSBuild", "VS.Solution", "jb.nuget.installer")
    }
}