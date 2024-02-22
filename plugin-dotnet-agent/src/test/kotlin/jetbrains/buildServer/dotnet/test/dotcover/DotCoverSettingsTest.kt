package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.runner.BuildInfo
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotcover.DotCoverMode
import jetbrains.buildServer.dotcover.DotCoverModeDetector
import jetbrains.buildServer.dotcover.DotCoverSettings
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverSettingsTest {

    @MockK
    private lateinit var _parametersService: ParametersService
    @MockK
    private lateinit var _dotCoverModeDetector: DotCoverModeDetector
    @MockK
    private lateinit var _buildInfo: BuildInfo
    @MockK
    private lateinit var _buildStepContext: BuildStepContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun `data provider when current step is equal to the last build step with dotCover`() = arrayOf(
        arrayOf(DotCoverMode.Wrapper, ParameterState.Enabled, true),
        arrayOf(DotCoverMode.Wrapper, ParameterState.Disabled, false),
        arrayOf(DotCoverMode.Wrapper, ParameterState.Missing, true),

        arrayOf(DotCoverMode.Runner, ParameterState.Enabled, true),
        arrayOf(DotCoverMode.Runner, ParameterState.Disabled, false),
        arrayOf(DotCoverMode.Runner, ParameterState.Missing, false),

        arrayOf(DotCoverMode.Disabled, ParameterState.Enabled, false),
        arrayOf(DotCoverMode.Disabled, ParameterState.Disabled, false),
        arrayOf(DotCoverMode.Disabled, ParameterState.Missing, false),
    )
    @Test(dataProvider = "data provider when current step is equal to the last build step with dotCover")
    fun `should determine if merging snapshots is necessary when current step is equal to the last build step with dotCover`(
        dotCoverMode: DotCoverMode,
        mergeParameterState: ParameterState,
        expectedResult: Boolean
    ) {
        // arrange
        val settings = createInstance()
        val runType = getRunTypeByMode(dotCoverMode)
        val runnerParameters = mapOf(
            CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER,
            "dotNetCoverage.dotCover.enabled" to ""
        )
        every { _buildStepContext.runnerContext.runType } returns runType
        every { _buildStepContext.runnerContext.runnerParameters } returns runnerParameters
        every { _dotCoverModeDetector.detectMode(runType, runnerParameters) } returns dotCoverMode

        when (mergeParameterState) {
            ParameterState.Enabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED) } returns "true"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "true"
            }
            ParameterState.Disabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED) } returns "false"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "false"
            }
            ParameterState.Missing -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED) } returns null
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns null
            }
        }

        val step = "curentStep"
        every { _buildInfo.id } returns step
        settings.lastBuildStepIdWithDotCoverEnabled = step

        // act
        val result = settings.shouldMergeSnapshots()

        // assert
        if (expectedResult) {
            assertEquals(result.first, true)
            assertEquals(result.second, "")
        } else {
            assertEquals(result.first, false)
            assertEquals(result.second, "Merging dotCover snapshots is disabled; skipping this stage")
        }
    }

    @Test(dataProvider = "data provider when current step is equal to the last build step with dotCover")
    fun `should determine if generating a report is necessary when current step is equal to the last build step with dotCover`(
        dotCoverMode: DotCoverMode,
        reportParameterState: ParameterState,
        expectedResult: Boolean
    ) {
        // arrange
        val settings = createInstance()
        val runType = getRunTypeByMode(dotCoverMode)
        val runnerParameters = mapOf(
            CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER,
            "dotNetCoverage.dotCover.enabled" to ""
        )
        every { _buildStepContext.runnerContext.runType } returns runType
        every { _buildStepContext.runnerContext.runnerParameters } returns runnerParameters
        every { _dotCoverModeDetector.detectMode(runType, runnerParameters) } returns dotCoverMode

        when (reportParameterState) {
            ParameterState.Enabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED) } returns "true"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "true"
            }
            ParameterState.Disabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED) } returns "false"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "false"
            }
            ParameterState.Missing -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED) } returns null
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns null
            }
        }

        val step = "curentStep"
        every { _buildInfo.id } returns step
        settings.lastBuildStepIdWithDotCoverEnabled = step

        // act
        val result = settings.shouldGenerateReport()

        // assert
        if (expectedResult) {
            assertEquals(result.first, true)
            assertEquals(result.second, "")
        } else {
            assertEquals(result.first, false)
            assertEquals(result.second, "Building a coverage report is disabled; skipping this stage")
        }
    }

    @DataProvider
    fun `data provider when current step is not equal to the last build step with dotCover`() = arrayOf(
        arrayOf(DotCoverMode.Wrapper, ParameterState.Enabled),
        arrayOf(DotCoverMode.Wrapper, ParameterState.Disabled),
        arrayOf(DotCoverMode.Wrapper, ParameterState.Missing),

        arrayOf(DotCoverMode.Runner, ParameterState.Enabled),
        arrayOf(DotCoverMode.Runner, ParameterState.Disabled),
        arrayOf(DotCoverMode.Runner, ParameterState.Missing),

        arrayOf(DotCoverMode.Disabled, ParameterState.Enabled),
        arrayOf(DotCoverMode.Disabled, ParameterState.Disabled),
        arrayOf(DotCoverMode.Disabled, ParameterState.Missing)
    )
    @Test(dataProvider = "data provider when current step is not equal to the last build step with dotCover")
    fun `should determine if merging snapshots is necessary when current step is not equal to the last build step with dotCover`(
        dotCoverMode: DotCoverMode,
        mergeParameterState: ParameterState
    ) {
        // arrange
        val settings = createInstance()
        val runType = getRunTypeByMode(dotCoverMode)
        val runnerParameters = mapOf(
            CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER,
            "dotNetCoverage.dotCover.enabled" to ""
        )
        every { _buildStepContext.runnerContext.runType } returns runType
        every { _buildStepContext.runnerContext.runnerParameters } returns runnerParameters
        every { _dotCoverModeDetector.detectMode(runType, runnerParameters) } returns dotCoverMode

        when (mergeParameterState) {
            ParameterState.Enabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED) } returns "true"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "true"
            }
            ParameterState.Disabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED) } returns "false"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "false"
            }
            ParameterState.Missing -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED) } returns null
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns null
            }
        }

        every { _buildInfo.id } returns "curentStep"
        settings.lastBuildStepIdWithDotCoverEnabled = "anotherStep"

        // act
        val result = settings.shouldMergeSnapshots()

        // assert
        assertEquals(result.first, false)
    }

    @Test(dataProvider = "data provider when current step is not equal to the last build step with dotCover")
    fun `should determine if generating a report is necessary when current step is not equal to the last build step with dotCover`(
        dotCoverMode: DotCoverMode,
        reportParameterState: ParameterState
    ) {
        // arrange
        val settings = createInstance()
        val runType = getRunTypeByMode(dotCoverMode)
        val runnerParameters = mapOf(
            CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER,
            "dotNetCoverage.dotCover.enabled" to ""
        )
        every { _buildStepContext.runnerContext.runType } returns runType
        every { _buildStepContext.runnerContext.runnerParameters } returns runnerParameters
        every { _dotCoverModeDetector.detectMode(runType, runnerParameters) } returns dotCoverMode

        when (reportParameterState) {
            ParameterState.Enabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED) } returns "true"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "true"
            }
            ParameterState.Disabled -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED) } returns "false"
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns "false"
            }
            ParameterState.Missing -> {
                every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED) } returns null
                every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT) } returns null
            }
        }

        every { _buildInfo.id } returns "curentStep"
        settings.lastBuildStepIdWithDotCoverEnabled = "anotherStep"

        // act
        val result = settings.shouldGenerateReport()

        // assert
        assertEquals(result.first, false)
    }

    @DataProvider
    fun `build steps provider`() = arrayOf(
        arrayOf(
            listOf(BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_1", true)),
            "step_1"
        ),
        arrayOf(
            listOf(BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_1", false)),
            null
        ),
        arrayOf(
            emptyList<BuildRunnerSettings>(),
            null
        ),
        arrayOf(
            listOf(
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_1", true),
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_2", true),
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_3", true),
            ),
            "step_3"
        ),
        arrayOf(
            listOf(
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_1", true),
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_2", true),
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_3", false),
            ),
            "step_2"
        ),
        arrayOf(
            listOf(
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_1", false),
                BuildRunnerSettingsMock(CoverageConstants.DOTCOVER_RUNNER_TYPE, "step_2", false),
            ),
            null
        ),
    )
    @Test(dataProvider = "build steps provider")
    fun `should detect the last step with dotCover`(steps: List<BuildRunnerSettings>, expectedResult: String?) {
        // arrange
        val settings = createInstance()
        every { _dotCoverModeDetector.detectMode(CoverageConstants.DOTCOVER_RUNNER_TYPE, mutableMapOf()) } returns DotCoverMode.Runner

        // act
        val result = settings.findLastBuildStepIdWithDotCoverEnabled(steps)

        // assert
        assertEquals(result, expectedResult)
    }

    private fun getRunTypeByMode(dotCoverMode: DotCoverMode) = when (dotCoverMode) {
        DotCoverMode.Runner -> CoverageConstants.DOTCOVER_RUNNER_TYPE
        DotCoverMode.Wrapper -> DotnetConstants.RUNNER_TYPE
        DotCoverMode.Disabled -> ""
    }

    private fun createInstance(): DotCoverSettings {
        return DotCoverSettings(
            _parametersService,
            _dotCoverModeDetector,
            _buildInfo,
            _buildStepContext,
            EventDispatcher.create(AgentLifeCycleListener::class.java)
        )
    }

    enum class ParameterState {
        Enabled,
        Disabled,
        Missing
    }

    class BuildRunnerSettingsMock(
        private val runType: String,
        private val stepId: String,
        private val isEnabled: Boolean
    ) : BuildRunnerSettings {
        override fun getRunType(): String {
            return runType
        }

        override fun getName(): String {
            return stepId
        }

        override fun getId(): String {
            return stepId
        }

        override fun isEnabled(): Boolean {
            return isEnabled
        }

        override fun hasChildren(): Boolean {
            return false
        }

        override fun getChildren(): MutableList<BuildRunnerSettings> {
            return mutableListOf()
        }

        override fun getParent(): BuildRunnerSettings? {
            return null
        }

        override fun getRunnerParameters(): MutableMap<String, String> {
            return mutableMapOf()
        }

        override fun getBuildParameters(): MutableMap<String, String> {
            return mutableMapOf()
        }

        override fun getConfigParameters(): MutableMap<String, String> {
            return mutableMapOf()
        }

        override fun getWorkingDirectory(): String {
            return ""
        }
    }
}