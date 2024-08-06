package jetbrains.buildServer.dotCover

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.*
import jetbrains.buildServer.dotnet.CoverageConstants.BUNDLED_TOOL_VERSION
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_PACKAGE_ID
import jetbrains.buildServer.dotnet.SemanticVersion
import jetbrains.buildServer.tools.ToolException
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.testng.Assert
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverToolProviderAdapterTest {

    @MockK
    private lateinit var _toolService: ToolService

    @MockK(relaxed = true)
    private lateinit var _toolType: ToolType

    @MockK
    private lateinit var _toolComparator: DotCoverToolComparator

    @MockK
    private lateinit var _toolFilter: DotCoverPackageFilter

    @MockK
    private lateinit var _packageIdResolver: DotCoverPackageIdResolver

    @MockK
    private lateinit var _pluginDescriptor: PluginDescriptor

    @MockK
    private lateinit var _fileSystem: FileSystemService

    private lateinit var _xmlDocumentService: XmlDocumentService

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)

        _xmlDocumentService = XmlDocumentServiceImpl()
    }

    @Test
    fun `should parse dotCover nuspec file retrieving id and version`() {
        // arrange
        val source = DotCoverToolProviderAdapterTest::class.java
            .getResource("JetBrains.dotCover.CommandLineTools.nuspec")!!.openStream()

        // act
        val result = _xmlDocumentService.deserialize(source)
        val bundledPackageId = DotCoverToolProviderAdapter
            .getContents(result, "/package/metadata/id").firstOrNull()
        val bundledPackageVersion = DotCoverToolProviderAdapter
            .getContents(result, "/package/metadata/version").firstOrNull()

        // assert
        Assert.assertNotNull(bundledPackageId)
        Assert.assertNotNull(bundledPackageVersion)
        Assert.assertEquals(bundledPackageId, "JetBrains.dotCover.CommandLineTools")
        Assert.assertEquals(bundledPackageVersion, "2023.3.0-tc03")
    }

    @Test
    fun `should get available tool versions in sorted order`() {
        // arrange
        val toolProviderAdapter = createInstance()
        every {
            _toolService.getPackages(*DOT_COVER_PACKAGES)
        } returns listOf(
            NuGetPackage("id1", "2022.1.0", mockk(relaxed = true)),
            NuGetPackage("id2", "2021.9.0", mockk(relaxed = true)),
            NuGetPackage("id3", "2023.2.0", mockk(relaxed = true)),
        )
        every { _toolFilter.accept(any()) } returns true
        every { _toolComparator.compare(any(), any()) } answers { callOriginal() }
        every { _toolComparator.reversed() } answers { callOriginal() }

        // act
        val availableToolVersions = toolProviderAdapter.availableToolVersions

        // assert
        Assert.assertEquals(
            availableToolVersions.map { it.version }.toList(),
            listOf("2023.2.0", "2022.1.0", "2021.9.0")
        )
    }

    @DataProvider
    fun testDataUnpackToolPackage(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(SemanticVersion(2023, 2, 9), DOTCOVER_PACKAGE_ID, false),
            arrayOf(SemanticVersion(2023, 2, 9), DOTCOVER_DEPRECATED_PACKAGE_ID, false),
            arrayOf(SemanticVersion(2023, 3, 0, "eap09"), DOTCOVER_PACKAGE_ID, false),
            arrayOf(SemanticVersion(2023, 3, 0, "eap09"), DOTCOVER_DEPRECATED_PACKAGE_ID, false),
            arrayOf(SemanticVersion(2023, 3, 0, "tc09"), DOTCOVER_PACKAGE_ID, false),
            arrayOf(SemanticVersion(2023, 3, 0, "tc09"), DOTCOVER_DEPRECATED_PACKAGE_ID, false),
            arrayOf(SemanticVersion(2023, 3, 0), DOTCOVER_PACKAGE_ID, true),
            arrayOf(SemanticVersion(2023, 3, 0), DOTCOVER_DEPRECATED_PACKAGE_ID, true),
            arrayOf(SemanticVersion(2023, 3, 1), DOTCOVER_PACKAGE_ID, true),
            arrayOf(SemanticVersion(2023, 3, 1), DOTCOVER_DEPRECATED_PACKAGE_ID, true),
            arrayOf(SemanticVersion(2023, 4, 0), DOTCOVER_PACKAGE_ID, true),
            arrayOf(SemanticVersion(2023, 4, 0), DOTCOVER_DEPRECATED_PACKAGE_ID, true),
        )
    }

    @Test(dataProvider = "testDataUnpackToolPackage")
    fun `should unpack tool package preserving the teamcity plugin xml descriptor when needed`(
        version: SemanticVersion,
        packageId: String,
        shouldPreserveDescriptor: Boolean,
    ) {
        // arrange
        val toolProviderAdapter = createInstance()
        val toolPackage = File("toolPackage")
        val targetDirectory = File("targetDirectory")
        val pluginRoot = File("pluginRoot")
        every { _toolService.unpackToolPackage(any(), any(), any(), *anyVararg<String>()) } just runs
        every { _toolComparator.compare(any(), any()) } answers { callOriginal() }
        every { _toolService.getPackageVersion(any(), *anyVararg<String>()) } returns version
        every { _packageIdResolver.resolvePackageId(any()) } returns packageId
        every { _pluginDescriptor.pluginRoot } returns pluginRoot
        every { _fileSystem.isExists(any()) } returns true
        every { _fileSystem.copy(any(), any()) } just runs

        // act
        toolProviderAdapter.unpackToolPackage(toolPackage, targetDirectory)

        // assert
        verify(exactly = 1) {
            _toolService.unpackToolPackage(toolPackage, "", targetDirectory, *DOT_COVER_PACKAGES)
        }
        if (shouldPreserveDescriptor) {
            verify(exactly = 0) { _fileSystem.copy(any(), any()) }
        } else {
            verify(exactly = 1) {
                _fileSystem.copy(
                    File(pluginRoot, DOT_COVER_DEPRECATED_PLUGIN_XML_PATH),
                    File(targetDirectory, "teamcity-plugin.xml")
                )
            }
        }
    }

    @Test
    fun `should return link to the bundled dotCover in the error message`() {
        // arrange
        every { _toolService.getPackages(*DOT_COVER_PACKAGES) } throws ToolException("")
        every { _toolFilter.accept(any()) } returns true
        every { _toolComparator.compare(any(), any()) } answers { callOriginal() }
        every { _toolComparator.reversed() } answers { callOriginal() }
        val toolVersion = DotCoverToolVersion(DotCoverToolTypeAdapter(), BUNDLED_TOOL_VERSION, DOTCOVER_PACKAGE_ID)
        val toolPackage = File("testToolPackage")
        val assertionError = "Expected ToolException with proper link"

        try {
            // act
            createInstance().fetchToolPackage(toolVersion, toolPackage)
            Assert.fail(assertionError)
        } catch (e: Exception) {
            if (e !is ToolException) {
                Assert.fail(assertionError)
            }

            // assert
            Assert.assertNotNull(e.message)
            assertTrue(e.message!!.contains("https://www.nuget.org/api/v2/package/JetBrains.dotCover.CommandLineTools/$BUNDLED_TOOL_VERSION"))
        }
    }

    private fun createInstance() = DotCoverToolProviderAdapter(
        _toolService,
        _toolType,
        _toolComparator,
        _toolFilter,
        _packageIdResolver,
        _pluginDescriptor,
        _fileSystem,
        _xmlDocumentService,
    )

    companion object {
        private const val DOT_COVER_DEPRECATED_PLUGIN_XML_PATH =
            "server/tool-descriptors/dotcover-teamcity-plugin.xml"
        private val DOT_COVER_PACKAGES = arrayOf(DOTCOVER_DEPRECATED_PACKAGE_ID, DOTCOVER_PACKAGE_ID)
    }
}