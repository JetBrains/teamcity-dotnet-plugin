package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.AgentBuildSettings
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher

// this class is not thread-safe since it
// supposed to be used in single build-step related thread
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

    private val skipProcessingForCurrentBuildStep get() = buildStepId != lastBuildStepIdWithDotCoverEnabled

    private val dotCoverMode get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_MODE)
            ?.trim()
            ?.lowercase()
            ?.let { DotCoverMode.fromString(it) }
            ?: DotCoverMode.Wrapper // TODO not sure that's right... Should we throw an exception?

    val buildLogger get() = _buildStepContext.runnerContext.build.buildLogger

    val configParameters get() = _buildStepContext.runnerContext.build.sharedConfigParameters

    val buildStepId get() = _buildInfo.id

    val coveragePostProcessingEnabled get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_COVERAGE_DATA_POST_PROCESSING_ENABLED)
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: false

    fun shouldMergeSnapshots() =
        when (dotCoverMode) {
            DotCoverMode.Wrapper ->
                _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED)
                    ?.toBooleanStrictOrNull()
                    ?: true
            DotCoverMode.Runner ->
                _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT)
                    ?.toBooleanStrictOrNull()
                    ?: true
        }.let { mergeParameterEnabled -> when {
            !mergeParameterEnabled -> {
                _loggerService.writeDebug("Merging dotCover snapshots is disabled; skipping this stage")
                false
            }
            skipProcessingForCurrentBuildStep -> {
                _loggerService.writeDebug("Merging dotCover snapshots is not supposed for this build step; skipping this stage")
                false
            }
            else -> true
        }}

    fun shouldGenerateReport() =
        when (dotCoverMode) {
            DotCoverMode.Wrapper ->
                _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED)
                    ?.toBooleanStrictOrNull()
                    ?: true
            DotCoverMode.Runner ->
                _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT)
                    ?.toBooleanStrictOrNull()
                    ?: true
        }.let { reportParameterEnabled -> when {
            !reportParameterEnabled -> {
                _loggerService.writeDebug("Building a coverage report is disabled; skipping this stage")
                false
            }
            skipProcessingForCurrentBuildStep -> {
                _loggerService.writeDebug("Building a coverage report is is not supposed for this build step; skipping this stage")
                false
            }
            else -> true
        }}

    private fun findLastBuildStepIdWithDotCoverEnabled(runningBuild: AgentRunningBuild) =
        runningBuild.buildRunners
            .filter { it.hasDotCoverEnabled() }
            .map { it.id }
            .lastOrNull()

    companion object {
        private fun BuildRunnerSettings.hasDotCoverEnabled() = when {
            this.isEnabled -> when (this.runType) {
                DotnetConstants.RUNNER_TYPE ->
                    this.runnerParameters[CoverageConstants.PARAM_TYPE] == CoverageConstants.PARAM_DOTCOVER
                CoverageConstants.PARAM_DOTCOVER_RUNNER_TYPE -> true
                else -> false
            }
            else -> false
        }
    }
}