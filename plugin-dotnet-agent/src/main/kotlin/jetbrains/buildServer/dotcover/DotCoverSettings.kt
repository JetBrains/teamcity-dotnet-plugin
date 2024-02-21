package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

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
                if (coveragePostProcessingEnabled) {
                    return
                }

                lastBuildStepIdWithDotCoverEnabled = findLastBuildStepIdWithDotCoverEnabled(runningBuild)
                lastBuildStepIdWithDotCoverEnabled?.let {
                    // The changes from this commit are a temporary solution.
                    // They can be removed after moving the NUnit and MSpec runners to the teamcity-dotnet-plugin.
                    // This is necessary in order to make dotCover reports from the NUnit runner compatible with dotCover reports from .NET / dotCover runners.
                    // This parameter allows the NUnit runner to recognize that report generation will be performed as part of a .NET / dotCover step,
                    // and not to initiate coverage reports post-processing, which would lead to errors in the build log
                    // see jetbrains.buildServer.dotNet.testRunner.agent.DotCoverSetupBuilder
                    runningBuild.addSharedConfigParameter("dotCover.reportProcessingBuildStepId", it)
                }
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

    val dotCoverHomePath get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            .let { when (it.isNullOrBlank()) {
                true -> ""
                false -> it
            }}

    val buildLogger get() = _buildStepContext.runnerContext.build.buildLogger

    val configParameters get() = _buildStepContext.runnerContext.build.sharedConfigParameters

    val buildStepId get() = _buildInfo.id

    val coveragePostProcessingEnabled get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_COVERAGE_DATA_POST_PROCESSING_ENABLED)
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: false

    // TODO resolve wildcards
    val additionalSnapshotPaths: List<String> get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS)
            ?.trim()?.split('\n')?.filterNot { it.isNullOrBlank() } ?: emptyList()

    fun shouldMergeSnapshots(): Pair<Boolean, String> =
        when (dotCoverMode) {
            DotCoverMode.Wrapper ->
                _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED)
                    ?.toBooleanStrictOrNull()
                    ?: true
            DotCoverMode.Runner ->
                _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT)
                    ?.toBooleanStrictOrNull()
                    ?: false
            DotCoverMode.Disabled -> false
        }.let { mergeParameterEnabled -> when {
            !mergeParameterEnabled -> false to "Merging dotCover snapshots is disabled; skipping this stage"
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
                    ?: false
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