package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.mockk
import jetbrains.buildServer.dotcover.DotCoverMode
import jetbrains.buildServer.dotcover.DotCoverModeDetector
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverModeDetectorTest {
    private val _instance = DotCoverModeDetector()

    @DataProvider
    fun `coverage types and expected dotCover mode`() = arrayOf(
        arrayOf(DotCoverMode.Runner, CoverageConstants.DOTCOVER_RUNNER_TYPE, emptyMap<String, String>()),
        arrayOf(DotCoverMode.Wrapper, DotnetConstants.RUNNER_TYPE, mapOf(
            CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER
        )),
        arrayOf(DotCoverMode.Wrapper, DotnetConstants.RUNNER_TYPE, mapOf(
            CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER,
            "dotNetCoverage.dotCover.enabled" to ""
        )),
        arrayOf(DotCoverMode.Wrapper, DotnetConstants.RUNNER_TYPE, mapOf(
            CoverageConstants.PARAM_TYPE to "UNKNOWN",
            "dotNetCoverage.dotCover.enabled" to "true"
        )),
        arrayOf(DotCoverMode.Wrapper, DotnetConstants.RUNNER_TYPE, mapOf(
            "dotNetCoverage.dotCover.enabled" to "true"
        )),
        arrayOf(DotCoverMode.Disabled, DotnetConstants.RUNNER_TYPE, mapOf(
            CoverageConstants.PARAM_TYPE to "UNKNOWN"
        )),
        arrayOf(DotCoverMode.Disabled, DotnetConstants.RUNNER_TYPE, mapOf(
            "dotNetCoverage.dotCover.enabled" to null
        )),
        arrayOf(DotCoverMode.Disabled, "UNKNOWN", emptyMap<String, String>()),
    )
    @Test(dataProvider = "coverage types and expected dotCover mode")
    fun `should detect dotCover mode by runner type and runner parameters`(
        expecedMode: DotCoverMode,
        runnerType: String,
        runParameters: Map<String, String>
    ) {
        // act
        val actualMode = _instance.detectMode(runnerType, runParameters)

        // assert
        assertEquals(actualMode, expecedMode)
    }
}