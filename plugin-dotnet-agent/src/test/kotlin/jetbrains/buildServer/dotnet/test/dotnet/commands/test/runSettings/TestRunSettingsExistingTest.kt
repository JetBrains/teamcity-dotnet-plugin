

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.runSettings

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.Deserializer
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsExisting
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.w3c.dom.Document
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

class TestRunSettingsExistingTest {
    private val _settingsFile11 = File("1.settings")
    private val _settingsFile1 = File("WD", "1.settings")
    private val _settingsFile2 = File("2.settings")
    private val _documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    private val _settings1 = _documentBuilder.newDocument()
    private val _settings2 = _documentBuilder.newDocument()
    private val _newSettings = _documentBuilder.newDocument()
    @MockK private lateinit var _fileSystem: FileSystemService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _deserializer: Deserializer<Document>
    @MockK private lateinit var _xmlDocumentService: XmlDocumentService
    @MockK private lateinit var _testRunSettingsFileProvider1: TestRunSettingsFileProvider
    @MockK private lateinit var _testRunSettingsFileProvider2: TestRunSettingsFileProvider
    @MockK private lateinit var _inputStream1: InputStream
    @MockK private lateinit var _inputStream2: InputStream
    @MockK private lateinit var _commandContext: DotnetCommandContext
    private lateinit var _fileProviders: List<TestRunSettingsFileProvider>

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _commandContext.command.commandType } returns DotnetCommandType.Test
        every { _testRunSettingsFileProvider1.tryGet(match { it.command.commandType == DotnetCommandType.Test }) } returns _settingsFile11
        every { _fileSystem.isAbsolute(_settingsFile11) } returns false
        every { _fileSystem.isExists(_settingsFile1) } returns true
        every { _fileSystem.isFile(_settingsFile1) } returns true
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File("WD")
        every { _fileSystem.read<Document>(_settingsFile1, any()) } answers {
            arg<(InputStream) -> Document>(1).invoke(_inputStream1)
            _settings1
        }
        every { _deserializer.deserialize(_inputStream1) } returns _settings1

        every { _testRunSettingsFileProvider2.tryGet(match { it.command.commandType == DotnetCommandType.Test }) } returns _settingsFile2
        every { _fileSystem.isAbsolute(_settingsFile2) } returns true
        every { _fileSystem.isExists(_settingsFile2) } returns true
        every { _fileSystem.isFile(_settingsFile2) } returns true
        every { _fileSystem.read<Document>(_settingsFile2, any()) } answers {
            arg<(InputStream) -> Document>(1).invoke(_inputStream2)
            _settings2
        }
        every { _deserializer.deserialize(_inputStream2) } returns _settings2

        _fileProviders = listOf(_testRunSettingsFileProvider1, _testRunSettingsFileProvider2)
    }

    @Test
    public fun shouldProvideFirstAvailableExistingSettingsAccordingToSpecificOrderInListOfFileProviders() {
        // Given
        val provider = createInstance()

        // When
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _settings1)
        verify { _deserializer.deserialize(_inputStream1) }
    }

    @Test
    public fun shouldProvideFirstAvailableExistingSettingsWhenFirstIsUnavailable() {
        // Given
        val provider = createInstance()

        // When
        every { _testRunSettingsFileProvider1.tryGet(any()) } returns null
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _settings2)
        verify { _deserializer.deserialize(_inputStream2) }
    }

    @Test
    public fun shouldProvideFirstAvailableExistingSettingsWhenFirstFileDoesNotExist() {
        // Given
        val provider = createInstance()

        // When
        every { _fileSystem.isExists(_settingsFile1) } returns false
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _settings2)
        verify { _deserializer.deserialize(_inputStream2) }
    }

    @Test
    public fun shouldProvideFirstAvailableExistingSettingsWhenFirstFileIsNotFile() {
        // Given
        val provider = createInstance()

        // When
        every { _fileSystem.isFile(_settingsFile1) } returns false
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _settings2)
        verify { _deserializer.deserialize(_inputStream2) }
    }

    @Test
    public fun shouldProvideFirstAvailableExistingSettingsWhenFirstThrowsError() {
        // Given
        val provider = createInstance()

        // When
        every { _testRunSettingsFileProvider1.tryGet(any()) } throws Throwable()
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _settings2)
        verify { _deserializer.deserialize(_inputStream2) }
    }

    @Test
    public fun shouldProvideFirstAvailableExistingSettingsWhenCannotRead() {
        // Given
        val provider = createInstance()

        // When
        every { _fileSystem.read<Document>(_settingsFile1, any()) } throws Throwable()
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _settings2)
        verify { _deserializer.deserialize(_inputStream2) }
    }

    @Test
    public fun shouldProvideFirstAvailableExistingSettingsWhenCannotDeserialize() {
        // Given
        val provider = createInstance()

        // When
        every { _deserializer.deserialize(_inputStream1) } throws Throwable()
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _settings2)
        verify { _deserializer.deserialize(_inputStream2) }
    }

    @Test
    public fun shouldProvideNewSettingsWhenThereAreNoExistingSettings() {
        // Given
        val provider = createInstance()

        // When
        every { _testRunSettingsFileProvider1.tryGet(any()) } returns null
        every { _testRunSettingsFileProvider2.tryGet(any()) } returns null
        every { _xmlDocumentService.create() } returns _newSettings
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, _newSettings)
    }

    @Test
    public fun shouldNotProvideAnySettingsWhenCannotCreateNewOne() {
        // Given
        val provider = createInstance()

        // When
        every { _testRunSettingsFileProvider1.tryGet(any()) } returns null
        every { _testRunSettingsFileProvider2.tryGet(any()) } returns null
        every { _xmlDocumentService.create() } throws Throwable()
        val actualSettings = provider.tryCreate(_commandContext)

        // Then
        Assert.assertEquals(actualSettings, null)
    }

    private fun createInstance() = TestRunSettingsExisting(_fileProviders, _fileSystem, _pathsService, _deserializer, _xmlDocumentService)
}