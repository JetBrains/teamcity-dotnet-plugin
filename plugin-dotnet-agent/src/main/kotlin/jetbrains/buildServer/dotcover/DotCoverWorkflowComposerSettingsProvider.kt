package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher

class DotCoverWorkflowComposerSettingsProvider(
    private val _parametersService: ParametersService,
    private val _buildInfo: BuildInfo,
    private val _buildStepContext: BuildStepContext,
    private val _events: EventDispatcher<AgentLifeCycleListener?>,
    private val _loggerService: LoggerService
) {

    init {
        _events.addListener(object : AgentLifeCycleAdapter() {
            override fun buildStarted(runningBuild: AgentRunningBuild) {
                lastBuildStepIdWithDotCoverEnabled = findLastBuildStepIdWithDotCoverEnabled(runningBuild)
            }
            override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
                lastBuildStepIdWithDotCoverEnabled = null
            }
        })
    }

    private var lastBuildStepIdWithDotCoverEnabled: String? = null

    private val dotCoverMode get() = DotCoverMode.Wrapper

    val buildLogger get() = _buildStepContext.runnerContext.build.buildLogger

    val configParameters: Map<String, String> get() = _buildStepContext.runnerContext.build.sharedConfigParameters

    val buildStepId get() = _buildInfo.id

    val coveragePostProcessingEnabled
        get(): Boolean = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_COVERAGE_DATA_POST_PROCESSING_ENABLED)
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: false

    fun shouldMergeSnapshots(): Boolean {
        return when (dotCoverMode) {
            DotCoverMode.Wrapper -> {
                val mergePropertyEnabled = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED)
                    ?.toBooleanStrictOrNull()
                    ?: true
                if (!mergePropertyEnabled) {
                    _loggerService.writeDebug("Merging snapshots is disabled; skipping this stage")
                    return false
                }
                if (buildStepId != lastBuildStepIdWithDotCoverEnabled) {
                    _loggerService.writeDebug("Merging snapshots is not supposed for this build step; skipping this stage")
                    return false
                }
                return true
            }
            DotCoverMode.Runner -> _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_MERGE_SNAPSHOTS)
                ?.toBooleanStrictOrNull()
                ?: true
        }
    }

    fun shouldGenerateReport(): Boolean {
        return when (dotCoverMode) {
            DotCoverMode.Wrapper -> {
                val reportPropertyEnabled = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED)
                    ?.toBooleanStrictOrNull()
                    ?: true
                if (!reportPropertyEnabled) {
                    _loggerService.writeDebug("Building a coverage report is disabled; skipping this stage")
                    return false
                }
                if (buildStepId != lastBuildStepIdWithDotCoverEnabled) {
                    _loggerService.writeDebug("Building a coverage report is is not supposed for this build step; skipping this stage")
                }
                return true
            }
            DotCoverMode.Runner -> _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT)
                ?.toBooleanStrictOrNull()
                ?: true
        }
    }

    private fun findLastBuildStepIdWithDotCoverEnabled(runningBuild: AgentRunningBuild): String? {
        return runningBuild.buildRunners
            .filter { it.runType == DotnetConstants.RUNNER_TYPE }
            .filter { it.isEnabled }
            .filter { it.runnerParameters[CoverageConstants.PARAM_TYPE] == CoverageConstants.PARAM_DOTCOVER }
            .map { it.id }
            .lastOrNull()
    }
}