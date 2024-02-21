package jetbrains.buildServer.dotCover

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.mockk
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverParametersPreprocessorTest {
    private lateinit var _instance: DotCoverParametersPreprocessor;

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this, relaxed = true)
        _instance = DotCoverParametersPreprocessor()
    }

    @DataProvider
    fun `run parameters and if they should be fixed`() = arrayOf(
        arrayOf(
            mutableMapOf(
                CoverageConstants.PARAM_TYPE to CoverageConstants.PARAM_DOTCOVER,
                CoverageConstants.PARAM_DOTCOVER_HOME to "/path/to/dotCover/home"
            ),
            false
        ),
        arrayOf(
            mutableMapOf(
                CoverageConstants.PARAM_TYPE to "abc",
                CoverageConstants.PARAM_DOTCOVER_HOME to "/path/to/dotCover/home"
            ),
            true
        ),
    )
    @Test(dataProvider = "run parameters and if they should be fixed")
    fun `should remove dotCover home parameter when dotCover disabled`(parameters: MutableMap<String, String>, shouldFix: Boolean) {
        // act
        _instance.fixRunBuildParameters(mockk(), parameters, mockk())

        // assert
        assertEquals(shouldFix, !parameters.contains(CoverageConstants.PARAM_DOTCOVER_HOME))
    }
}