package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.ReduceTestFailureFeedbackParameters
import jetbrains.buildServer.agent.runner.ParameterType.Runner
import jetbrains.buildServer.agent.runner.ParameterType.Configuration
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.testReordering.RunnerConfigVarTestInfoParser
import jetbrains.buildServer.nunit.testReordering.TestInfo
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class NUnitSettingsTest {
    @MockK
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _parametersService.tryGetParameter(any(), any()) } returns null
    }

    @Test
    fun `should return nunit settings`() {
        // arrange
        _parametersService.let {
            every { it.tryGetParameter(Runner, NUnitRunnerConstants.NUNIT_PATH) } returns "nunit-path"
            every { it.tryGetParameter(Runner, NUnitRunnerConstants.NUNIT_CATEGORY_INCLUDE) } returns "cats_in"
            every { it.tryGetParameter(Runner, NUnitRunnerConstants.NUNIT_CATEGORY_EXCLUDE) } returns "cats_ex"
            every { it.tryGetParameter(Runner, NUnitRunnerConstants.NUNIT_TESTS_FILES_EXCLUDE) } returns "files_ex"
            every { it.tryGetParameter(Runner, NUnitRunnerConstants.NUNIT_TESTS_FILES_INCLUDE) } returns "files_in"
            every { it.tryGetParameter(Runner, NUnitRunnerConstants.NUNIT_COMMAND_LINE) } returns "cmd"
            every { it.tryGetParameter(Runner, NUnitRunnerConstants.NUNIT_APP_CONFIG_FILE) } returns "app_conf"
            every { it.tryGetParameter(Runner, ReduceTestFailureFeedbackParameters.RUN_RISK_GROUP_TESTS_FIRST_PARAM) } returns "true"
            every { it.tryGetParameter(Runner, ReduceTestFailureFeedbackParameters.RECENTLY_FAILED_TESTS_PARAM) } returns
                    "TeamCity.NUnit.Category.STest.C_Test103_c1"

            every { it.tryGetParameter(Configuration, NUnitRunnerConstants.NUNIT_USES_PROJECT_FILE) } returns "false"
        }

        // act, assert
        createSettings().let {
            assertEquals(it.nUnitPath, "nunit-path")
            assertEquals(it.includeCategories, "cats_in")
            assertEquals(it.excludeCategories, "cats_ex")
            assertEquals(it.includeTestFiles, "files_in")
            assertEquals(it.excludeTestFiles, "files_ex")
            assertEquals(it.additionalCommandLine, "cmd")
            assertEquals(it.appConfigFile, "app_conf")
            assertEquals(
                it.testReorderingRecentlyFailedTests,
                listOf(TestInfo("TeamCity.NUnit.Category.STest.C_Test103_c1"))
            )
            assertFalse(it.useProjectFile)
            assertTrue(it.testReorderingEnabled)
        }
    }

    @Test
    fun `should return default nunit settings`() {
        // arrange
        val settings = createSettings()

        // act, assert
        assertEquals(settings.testReorderingRecentlyFailedTests, emptyList<TestInfo>())
        assertFalse(settings.useProjectFile)
        assertFalse(settings.testReorderingEnabled)
    }


    private fun createSettings() = NUnitSettingsImpl(_parametersService, RunnerConfigVarTestInfoParser())
}