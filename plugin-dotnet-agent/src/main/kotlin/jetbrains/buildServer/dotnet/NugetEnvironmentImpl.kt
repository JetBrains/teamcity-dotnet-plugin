package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.EventObserver
import jetbrains.buildServer.agent.EventSources
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.LoggerService
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
                    val dotnetStepName = _sameRunnersWithDockerWrapper.map { it.name }.firstOrNull { !it.isNullOrBlank() } ?: DotnetConstants.RUNNER_DISPLAY_NAME
                    _loggerService.writeWarning("The path to NuGet global cache was overridden by the \"$dotnetStepName\" build step. To prevent any issues with package restoration, use ${DotnetConstants.RUNNER_DISPLAY_NAME} runner instead of the current runner.\n")
                }
            }
        }

    private val _incompatibleRunners: List<BuildRunnerSettings> get() =
        _buildStepContext.runnerContext.build.buildRunners
                .filter { RestoringRunners.contains(it.runType) }

    private val _sameRunnersWithDockerWrapper: List<BuildRunnerSettings> get() =
        _buildStepContext.runnerContext.build.buildRunners
                .filter { DotnetConstants.RUNNER_TYPE.equals(it.runType, true) }
                .filter { !it.runnerParameters[DOCKER_WRAPPER_IMAGE_PARAM].isNullOrBlank() }

    companion object {
        internal const val DOCKER_WRAPPER_IMAGE_PARAM = "plugin.docker.imageId"
        internal val RestoringRunners = setOf("MSBuild", "VS.Solution", "jb.nuget.installer")
    }
}