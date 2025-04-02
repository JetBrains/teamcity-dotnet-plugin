package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.nunit.NUnitSettings
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import jetbrains.buildServer.nunit.toolState.NUnitToolStateVerifier
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NUnitToolStateVerifierTest {
    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    data class TestCase(
        val nUnitToolState: NUnitToolState,
        val errorExpected: Boolean,
        val missingExtensions: List<String> = emptyList()
    )

    @DataProvider(name = "nUnit3Cases")
    fun getCommandLineArgumentsTryInitializeCases() = arrayOf(
        TestCase(
            NUnitToolState(
                nUnitVersion = "3.4.1",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ), errorExpected = false
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.5.0",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ), errorExpected = false
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.6",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ), errorExpected = false
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "4",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ), errorExpected = false
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.4.1",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ),
            errorExpected = true,
            missingExtensions = listOf("NUnit.Engine.Listeners.TeamCityEventListener")
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.4.1",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ),
            errorExpected = true,
            missingExtensions = listOf(
                "NUnit.Engine.Listeners.TeamCityEventListener",
                "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader"
            )
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.5.0",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ),
            errorExpected = true,
            missingExtensions = listOf(
                "NUnit.Engine.Listeners.TeamCityEventListener",
                "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader"
            )
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.5.1.1",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ),
            errorExpected = true,
            missingExtensions = listOf(
                "NUnit.Engine.Listeners.TeamCityEventListener",
                "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader"
            )
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.4.0",
                extensions = mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            ), errorExpected = false
        ),

        TestCase(
            NUnitToolState(
                nUnitVersion = "3.2.1",
                extensions = mutableListOf()
            ),
            errorExpected = false
        )
    )

    @Test(dataProvider = "nUnit3Cases")
    fun `should check nunit version and extensions`(testCase: TestCase) {
        // arrange
        justRun { _loggerService.writeErrorOutput(any()) }
        every { _nUnitSettings.useProjectFile } returns true
        val service = NUnitToolStateVerifier(_loggerService, _nUnitSettings)

        // act
        service.verify(testCase.nUnitToolState)

        // assert
        val errorsExpected = if (testCase.errorExpected) 1 else 0
        verify(exactly = errorsExpected) {
            _loggerService.writeErrorOutput(match { error -> testCase.missingExtensions.all { error.contains(it) } })
        }
    }

    @Test
    fun `should require only teamcity event listener when project file is not used`() {
        // arrange
        justRun { _loggerService.writeErrorOutput(any()) }
        every { _nUnitSettings.useProjectFile } returns false
        val service = NUnitToolStateVerifier(_loggerService, _nUnitSettings)
        val nUnitToolState = NUnitToolState(
            nUnitVersion = "3.5.0",
            extensions = mutableListOf("NUnit.Engine.Listeners.TeamCityEventListener")
        )

        // act
        service.verify(nUnitToolState)

        // assert
        verify(exactly = 0) { _loggerService.writeErrorOutput(any()) }
    }
}