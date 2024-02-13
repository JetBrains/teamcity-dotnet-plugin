package jetbrains.buildServer.dotcover

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
class DotCoverSettings(
    private val _parametersService: ParametersService,
    private val _dotCoverModeDetector: DotCoverModeDetector,
    private val _buildInfo: BuildInfo,
    private val _buildStepContext: BuildStepContext,
    private val _events: EventDispatcher<AgentLifeCycleListener?>
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

    val dotCoverMode get() = _dotCoverModeDetector.detectMode(
        _buildStepContext.runnerContext.runType,
        _buildStepContext.runnerContext.runnerParameters
    )

    val buildLogger get() = _buildStepContext.runnerContext.build.buildLogger

    val configParameters get() = _buildStepContext.runnerContext.build.sharedConfigParameters

    val buildStepId get() = _buildInfo.id

    val coveragePostProcessingEnabled get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_COVERAGE_DATA_POST_PROCESSING_ENABLED)
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: false

    fun shouldMergeSnapshots(): Pair<Boolean, String> =
        when (dotCoverMode) {
            DotCoverMode.Wrapper ->
                _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED)
                    ?.toBooleanStrictOrNull()
                    ?: true
            DotCoverMode.Runner ->
                _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT)
                    ?.toBooleanStrictOrNull()
                    ?: true
            DotCoverMode.Disabled -> false
        }.let { mergeParameterEnabled -> when {
            !mergeParameterEnabled -> {
                false to "Merging dotCover snapshots is disabled; skipping this stage"
            }
            skipProcessingForCurrentBuildStep -> false to "Merging dotCover snapshots is not supposed for this build step; skipping this stage"
            else -> true to ""
        }}

    fun shouldGenerateReport(): Pair<Boolean, String> =
        when (dotCoverMode) {
            DotCoverMode.Wrapper ->
                _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED)
                    ?.toBooleanStrictOrNull()
                    ?: true
            DotCoverMode.Runner ->
                _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT)
                    ?.toBooleanStrictOrNull()
                    ?: true
            DotCoverMode.Disabled -> false
        }.let { reportParameterEnabled -> when {
            !reportParameterEnabled -> false to "Building a coverage report is disabled; skipping this stage"
            skipProcessingForCurrentBuildStep -> false to "Building a coverage report is is not supposed for this build step; skipping this stage"
            else -> true to ""
        }}

    private fun findLastBuildStepIdWithDotCoverEnabled(runningBuild: AgentRunningBuild) =
        runningBuild.buildRunners
            .filter { it.hasDotCoverEnabled() }
            .map { it.id }
            .lastOrNull()

    private fun BuildRunnerSettings.hasDotCoverEnabled() = when {
        this.isEnabled -> _dotCoverModeDetector.detectMode(this.runType, this.runnerParameters).isEnabled
        else -> false
    }
}