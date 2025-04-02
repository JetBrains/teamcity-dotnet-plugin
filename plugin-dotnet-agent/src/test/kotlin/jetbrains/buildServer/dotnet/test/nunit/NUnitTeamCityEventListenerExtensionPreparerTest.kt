package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.TempFiles
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.nunit.NUnitRunnerConstants
import jetbrains.buildServer.nunit.NUnitSettings
import jetbrains.buildServer.nunit.NUnitTeamCityEventListenerExtensionPreparer
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NUnitTeamCityEventListenerExtensionPreparerTest {
    @MockK
    private lateinit var _nUnitSettings: NUnitSettings
    @MockK
    private lateinit var _loggerService: LoggerService
    @MockK
    private lateinit var _pathsService: PathsService
    @MockK
    private lateinit var _fileSystemService: FileSystemService
    @MockK
    private lateinit var _parametersService: ParametersService

    private lateinit var tempFiles: TempFiles
    private lateinit var extensionPath: File

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        tempFiles = TempFiles()
        extensionPath = File(tempFiles.createTempDir(), "tools/NUnit.Extension.TeamCityEventListener")

        every { _loggerService.writeDebug(any()) } just Runs
        every { _parametersService.tryGetParameter(ParameterType.Configuration, NUnitRunnerConstants.OVERWRITE_TEAMCITY_EVENT_LISTENER) } returns null
    }

    @DataProvider(name = "versionsBefore4")
    fun getVersionsBefore4(): Array<String> = arrayOf(
        "3.20.0",
        "3.19.2",
        "3.19.1",
        "3.15.2",
        "3.7.0"
    )
    @Test(dataProvider = "versionsBefore4")
    fun `should not copy TeamCityEventListener for versions earlier than 4`(version: String) {
        // arrange
        val instance = create()
        val toolState = NUnitToolState(version, mutableListOf())

        // act
        instance.ensureExtensionPresent(toolState)

        // assert
        Assert.assertTrue(toolState.extensions.isEmpty())
        verify { _loggerService.writeDebug("For versions earlier than 4.0.0, TeamCityEventListener is expected to be included in the NUnit Console distribution. Skipping copy") }
        verify(exactly = 0) { _fileSystemService.copy(any(), any()) }
    }

    @DataProvider(name = "versions4AndHigher")
    fun getVersions4AndHigher(): Array<String> = arrayOf(
        "4.0.0",
        "4.0.32",
        "4.1.1"
    )
    @Test(dataProvider = "versions4AndHigher")
    fun `should copy TeamCityEventListener for versions 4 and higher`(version: String) {
        // arrange
        val instance = create()
        val toolState = NUnitToolState(version, mutableListOf())
        extensionPath.mkdirs()
        every { _nUnitSettings.nUnitPath } returns "/path/to/nunit"
        every { _pathsService.resolvePath(PathType.Plugin, any()) } returns extensionPath.toPath()
        every { _fileSystemService.copy(any(), any()) } just Runs

        // act
        instance.ensureExtensionPresent(toolState)

        // assert
        Assert.assertEquals(toolState.extensions.size, 1)
        Assert.assertEquals(toolState.extensions[0], "NUnit.Engine.Listeners.TeamCityEventListener")
        verify { _loggerService.writeDebug("TeamCityEventListener is no longer distributed as of version: $version. Copying it to the tool directory") }
        verify(exactly = 1) { _fileSystemService.copy(any(), any()) }
    }

    private fun create() = NUnitTeamCityEventListenerExtensionPreparer(
        _nUnitSettings, _loggerService, _pathsService, _fileSystemService, _parametersService
    )
}