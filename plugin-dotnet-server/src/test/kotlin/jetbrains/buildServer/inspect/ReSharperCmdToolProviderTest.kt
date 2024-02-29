package jetbrains.buildServer.inspect

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.*
import jetbrains.buildServer.inspect.CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
import jetbrains.buildServer.tools.GetPackageVersionResult.version
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.testng.Assert.*
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.net.URL
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

    private val toolTypeId = JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
    private val packageId = "JetBrains.ReSharper.CommandLineTools"
    private val bundledVersion = ReSharperToolVersion.BUNDLED_VERSION

    @BeforeMethod
    fun setUp() {
        _tempFiles = TempFiles()
        _toolService = mockk<ToolService>(relaxed = true)
        _toolType = mockk<ToolType>(relaxed = true)
        _fileSystemService = mockk<FileSystemService>(relaxed = true)
        _pluginDescriptor = mockk<PluginDescriptor>(relaxed = true)
        _xmlDocumentService = mockk<XmlDocumentService>(relaxed = true)

        every { _toolType.type } returns toolTypeId
    }

    @AfterMethod
    fun tearDown() {
        _tempFiles.cleanup()
    }

    @Test
    fun `should move unpacked tool package contents into the tools subdirectory if it does not exist`() {
        // Arrange
        val toolProvider = createToolProvider()

        every { _pluginDescriptor.pluginRoot } returns _tempFiles.createTempFile()

        val toolPackage = _tempFiles.createTempFile()
        val unpackedToolDirectory = _tempFiles.createTempDir()
        val toolFile = Paths.get(unpackedToolDirectory.absolutePath, "1.txt")
        Files.write(toolFile, listOf("foo"))

        // Act
        toolProvider.unpackToolPackage(toolPackage, unpackedToolDirectory)

        // Assert
        assertFalse(toolFile.exists())
        val movedToolFile = Paths.get(unpackedToolDirectory.absolutePath, "tools", "1.txt")
        assertTrue(movedToolFile.exists())
    }

    @Test
    fun `should not move unpacked tool package contents into the tools subdirectory if it already exists`() {
        // Arrange
        val toolProvider = createToolProvider()

        every { _pluginDescriptor.pluginRoot } returns _tempFiles.createTempFile()

        val toolPackage = _tempFiles.createTempFile()
        val unpackedToolDirectory = _tempFiles.createTempDir()
        val toolFile = Paths.get(unpackedToolDirectory.absolutePath, "1.txt")
        Files.write(toolFile, listOf("foo"))
        File(unpackedToolDirectory, "tools").mkdir()

        // Act
        toolProvider.unpackToolPackage(toolPackage, unpackedToolDirectory)

        // Assert
        assertTrue(toolFile.exists())
    }

    @DataProvider
    fun reSharperToolVersions(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("version", "$toolTypeId.version", false),
            arrayOf(bundledVersion, "$toolTypeId.bundled", true),
        )
    }

    @Test(dataProvider = "reSharperToolVersions")
    fun `should provide ReSharper tool data based on tool package`(
        version: String,
        expectedToolId: String,
        isBundled: Boolean,
    ) {
        // Arrange
        val toolProvider = createToolProvider()
        val nuGetTool = NuGetTool(_toolType, NuGetPackage(packageId, version, URL("https://api.nuget.org")))
        val toolPackage = _tempFiles.createTempFile()

        every { _toolService.tryGetPackageVersion(_toolType, toolPackage, packageId) } returns version(nuGetTool)

        // Act
        val result = toolProvider.tryGetPackageVersion(toolPackage)

        // Assert
        assertNull(result.details)
        assertNotNull(result.toolVersion)
        with(result.toolVersion!!) {
            assertTrue(this is ReSharperToolVersion)
            assertEquals(this.id, expectedToolId)
            assertEquals(this.originalId, "$toolTypeId.$version")
            assertEquals(this.version, version)
            assertEquals(this.isBundled, isBundled)
        }
    }

    @Test
    fun `should provide available ReSharper versions`() {
        // Arrange
        val toolProvider = createToolProvider()
        val version = "2022.2.2"
        val url = URL("https://api.nuget.org")
        every { _toolService.getTools(_toolType, packageId) } returns listOf(
            NuGetTool(_toolType, NuGetPackage(packageId, version, url)),
            NuGetTool(_toolType, NuGetPackage(packageId, "versionToSkip", url)),
        )

        // Act
        val result = toolProvider.availableToolVersions

        // Assert
        assertNotNull(result)
        assertEquals(result.size, 1)
        with(result.first()) {
            assertTrue(this is ReSharperToolVersion)
            assertEquals(this.id, "$toolTypeId.$version")
            assertEquals(this.version, version)
        }
    }

    @Test
    fun `should return bundled ReSharper tool as downloadable on TeamCity server startup`() {
        // Arrange
        val toolProvider = createToolProvider()

        every { _pluginDescriptor.pluginRoot } returns _tempFiles.createTempFile()

        // Act
        val bundledToPluginTool = toolProvider.bundledToolVersions
        val downloadableTool = toolProvider.downloadableBundledToolVersions

        // Assert
        assertEquals(bundledToPluginTool.size, 0)
        assertEquals(downloadableTool.size, 1)

        assertToolIsBundled(downloadableTool.first())
    }

    private fun assertToolIsBundled(tool: ToolVersion) {
        assertTrue(tool is ReSharperToolVersion)
        assertTrue(tool.isBundled)
        assertEquals(tool.version, bundledVersion)
        assertEquals(tool.id, "$toolTypeId.bundled")
        assertEquals(tool.originalId, "$toolTypeId.${tool.version}")
    }

    private fun createToolProvider() = ReSharperCmdToolProvider(
        packageId,
        _toolService,
        _toolType,
        _fileSystemService,
        _pluginDescriptor,
        _xmlDocumentService
    )
}