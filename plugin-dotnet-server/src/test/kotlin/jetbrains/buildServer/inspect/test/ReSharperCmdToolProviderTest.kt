package jetbrains.buildServer.inspect.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.FileSystemService
import jetbrains.buildServer.TempFiles
import jetbrains.buildServer.ToolService
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.inspect.ReSharperCmdToolProvider
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists

class ReSharperCmdToolProviderTest {
    private lateinit var _tempFiles: TempFiles
    private lateinit var _toolService: ToolService
    private lateinit var _toolType: ToolType
    private lateinit var _fileSystemService: FileSystemService
    private lateinit var _pluginDescriptor: PluginDescriptor
    private lateinit var _xmlDocumentService: XmlDocumentService

    @BeforeMethod
    fun setUp() {
        _tempFiles = TempFiles()
        _toolService = mockk<ToolService>(relaxed = true)
        _toolType = mockk<ToolType>(relaxed = true)
        _fileSystemService = mockk<FileSystemService>(relaxed = true)
        _pluginDescriptor = mockk<PluginDescriptor>(relaxed = true)
        _xmlDocumentService = mockk<XmlDocumentService>(relaxed = true)
    }

    @AfterMethod
    fun tearDown() {
        _tempFiles.cleanup()
    }

    @Test
    fun `should move unpacked tool package contents into the tools subdirectory if it does not exist`() {
        // Arrange
        val toolProvider = ReSharperCmdToolProvider(
            "packageId",
            _toolService,
            _toolType,
            _fileSystemService,
            _pluginDescriptor,
            _xmlDocumentService
        )

        every { _pluginDescriptor.pluginRoot } returns _tempFiles.createTempFile()

        val toolPackage = _tempFiles.createTempFile()
        val unpackedToolDirectory = _tempFiles.createTempDir()
        val toolFile = Paths.get(unpackedToolDirectory.absolutePath, "1.txt")
        Files.write(toolFile, listOf("foo"))

        // Act
        toolProvider.unpackToolPackage(toolPackage, unpackedToolDirectory)

        // Assert
        Assert.assertFalse(toolFile.exists())
        val movedToolFile = Paths.get(unpackedToolDirectory.absolutePath, "tools", "1.txt")
        Assert.assertTrue(movedToolFile.exists())
    }

    @Test
    fun `should not move unpacked tool package contents into the tools subdirectory if it already exists`() {
        // Arrange
        val toolProvider = ReSharperCmdToolProvider(
            "packageId",
            _toolService,
            _toolType,
            _fileSystemService,
            _pluginDescriptor,
            _xmlDocumentService
        )

        every { _pluginDescriptor.pluginRoot } returns _tempFiles.createTempFile()

        val toolPackage = _tempFiles.createTempFile()
        val unpackedToolDirectory = _tempFiles.createTempDir()
        val toolFile = Paths.get(unpackedToolDirectory.absolutePath, "1.txt")
        Files.write(toolFile, listOf("foo"))
        File(unpackedToolDirectory, "tools").mkdir()

        // Act
        toolProvider.unpackToolPackage(toolPackage, unpackedToolDirectory)

        // Assert
        Assert.assertTrue(toolFile.exists())
    }
}