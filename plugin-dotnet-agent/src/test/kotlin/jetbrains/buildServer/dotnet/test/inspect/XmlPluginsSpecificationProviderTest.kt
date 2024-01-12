

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.inspect.PluginDescriptorType.ID
import jetbrains.buildServer.inspect.PluginDescriptorType.SOURCE
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class XmlPluginsSpecificationProviderTest {
    @MockK
    private lateinit var _pluginDescriptorsProvider: PluginDescriptorsProvider

    @MockK
    private lateinit var _xmlWriter: XmlWriter

    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _folderPluginXmlElementGenerator: PluginXmlElementGenerator

    @MockK
    private lateinit var _filePluginXmlElementGenerator: PluginXmlElementGenerator

    @MockK
    private lateinit var _downloadPluginXmlElementGenerator: PluginXmlElementGenerator

    private val _folderSpec = "folderSpec"
    private val _fileSpec = "fileSpec"
    private val _downloadSpec = "downloadSpec"
    private val _folderElement = XmlElement("Folder", "MyFolder")
    private val _fileElement = XmlElement("File", "MyFile")
    private val _downloadElement = XmlElement("Download", "MyPlugin/MyVersion")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _loggerService.writeWarning(any()) } answers { }

        every { _folderPluginXmlElementGenerator.sourceId } returns "folder"
        every { _folderPluginXmlElementGenerator.generateXmlElement(any()) } returns XmlElement("Folder")
        every { _folderPluginXmlElementGenerator.generateXmlElement(_folderSpec) } returns _folderElement

        every { _filePluginXmlElementGenerator.sourceId } returns "file"
        every { _filePluginXmlElementGenerator.generateXmlElement(any()) } returns XmlElement("File")
        every { _filePluginXmlElementGenerator.generateXmlElement(_fileSpec) } returns _fileElement

        every { _downloadPluginXmlElementGenerator.sourceId } returns "download"
        every { _downloadPluginXmlElementGenerator.generateXmlElement(any()) } returns XmlElement("Download")
        every { _downloadPluginXmlElementGenerator.generateXmlElement(_downloadSpec) } returns _downloadElement
    }

    @DataProvider
    fun getCorrectPluginDescriptorsAndResults() = arrayOf(
        arrayOf(
            listOf(PluginDescriptor(SOURCE, "Folder $_folderSpec")),
            XmlElement("Packages", _folderElement)
        ),
        arrayOf(
            listOf(PluginDescriptor(SOURCE, "File $_fileSpec")),
            XmlElement("Packages", _fileElement)
        ),
        arrayOf(
            listOf(PluginDescriptor(SOURCE, "Download $_downloadSpec")),
            XmlElement("Packages", _downloadElement)
        ),
        arrayOf(
            listOf(
                PluginDescriptor(SOURCE, "folder $_folderSpec"),
                PluginDescriptor(SOURCE, "file $_fileSpec"),
                PluginDescriptor(SOURCE, "downLoad $_downloadSpec")
            ),
            XmlElement("Packages", _folderElement, _fileElement, _downloadElement)
        ),
        arrayOf(
            listOf(
                PluginDescriptor(SOURCE, "foLDER    $_folderSpec"),
                PluginDescriptor(SOURCE, "fILE   $_fileSpec"),
                PluginDescriptor(SOURCE, "dOwNlOaD   $_downloadElement")
            ),
            XmlElement("Packages", _folderElement, _fileElement, _downloadElement)
        )
    )

    @Test(dataProvider = "getCorrectPluginDescriptorsAndResults")
    fun `should create correct plugins specification in document format when having correct plugin descriptors`(
        pluginDescriptors: List<PluginDescriptor>,
        expectedPluginsSpecXmlElement: XmlElement?
    ) {
        // arrange
        val provider = createInstance()
        every { _pluginDescriptorsProvider.getPluginDescriptors() } answers { pluginDescriptors }
        every { _xmlWriter.write(any(), any()) } answers {
            val element = arg<XmlElement>(0)
            val stream = arg<OutputStream>(1)
            stream.writer().use {
                it.write(element.value ?: "")
            }
        }
        val expectedPluginsSpec: String? = serializeXmlElement(expectedPluginsSpecXmlElement)

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginDescriptorsProvider.getPluginDescriptors() }
        verify(exactly = 0) { _loggerService.writeWarning(any()) }
    }

    @DataProvider
    fun getIncorrectPluginDescriptors() = arrayOf(
        arrayOf(listOf(PluginDescriptor(SOURCE, "unknownType unknownSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "unknownType unknownSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "unknownType download unknownSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "unknownType file unknownSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "unknownType folder unknownSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "aDownload someSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "aFile someSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "aFolder someSpec"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "StyleCop.StyleCop/2021.2.1"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "download/StyleCop.StyleCop/2021.2.1"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "file/C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3.nupkg"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "folder/home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "download"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "file"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "folder"))),
        arrayOf(listOf(PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"))),
        arrayOf(listOf(PluginDescriptor(ID, "Download StyleCop.StyleCop/2021.2.1"))),
        arrayOf(listOf(PluginDescriptor(ID, "File C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3.nupkg"))),
        arrayOf(listOf(PluginDescriptor(ID, "Folder home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3"))),
        arrayOf(listOf(PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"), PluginDescriptor(SOURCE, "folder")))
    )

    @Test(dataProvider = "getIncorrectPluginDescriptors")
    fun `should emit warnings and not create specification when facing invalid plugin descriptors`(pluginDescriptors: List<PluginDescriptor>) {
        // arrange
        val provider = createInstance()
        every { _pluginDescriptorsProvider.getPluginDescriptors() } answers { pluginDescriptors }
        every { _xmlWriter.write(any(), any()) } answers {}

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertNull(actualPluginsSpec)
        verify(exactly = 1) { _pluginDescriptorsProvider.getPluginDescriptors() }
        verify(exactly = 0) { _xmlWriter.write(any(), any()) }
        verify(exactly = pluginDescriptors.size) { _loggerService.writeWarning(withArg { assertTrue(it.contains("it will be ignored")) }) }
    }

    @DataProvider
    fun getPartiallyCorrectPluginDescriptorsAndResults() = arrayOf(
        arrayOf(
            listOf(
                PluginDescriptor(SOURCE, "abc $_folderSpec"),
                PluginDescriptor(SOURCE, "filE $_fileSpec")
            ),
            XmlElement("Packages", _fileElement)
        ),
        arrayOf(
            listOf(
                PluginDescriptor(ID, "folder abc"),
                PluginDescriptor(SOURCE, "file $_fileSpec")
            ),
            XmlElement("Packages", _fileElement)
        ),
        arrayOf(
            listOf(
                PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"),
                PluginDescriptor(SOURCE, "file $_fileSpec")
            ),
            XmlElement("Packages", _fileElement)
        )
    )

    @Test(dataProvider = "getPartiallyCorrectPluginDescriptorsAndResults")
    fun `should create partial specification and emit warnings when facing both invalid and valid plugin descriptors`(
        pluginDescriptors: List<PluginDescriptor>,
        expectedPluginsSpecXmlElement: XmlElement?
    ) {
        // arrange
        val provider = createInstance()
        every { _pluginDescriptorsProvider.getPluginDescriptors() } answers { pluginDescriptors }
        every { _xmlWriter.write(any(), any()) } answers {
            val element = arg<XmlElement>(0)
            val stream = arg<OutputStream>(1)
            stream.writer().use {
                it.write(element.value ?: "")
            }
        }
        val expectedPluginsSpec: String? = serializeXmlElement(expectedPluginsSpecXmlElement)

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginDescriptorsProvider.getPluginDescriptors() }
        verify(exactly = 1) { _xmlWriter.write(any(), any()) }
        verify(exactly = 1) { _loggerService.writeWarning(withArg { assertTrue(it.contains("it will be ignored")) }) }
    }

    @Test
    fun `should return null specification when plugin descriptors list is empty`() {
        // arrange
        val provider = createInstance()
        every { _pluginDescriptorsProvider.getPluginDescriptors() } answers { emptyList() }

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertNull(actualPluginsSpec)
        verify(exactly = 1) { _pluginDescriptorsProvider.getPluginDescriptors() }
        verify(exactly = 0) { _xmlWriter.write(any(), any()) }
        verify(exactly = 0) { _loggerService.writeWarning(withArg { any() }) }
    }

    private fun serializeXmlElement(xmlElement: XmlElement?) = xmlElement?.let { spec ->
        ByteArrayOutputStream().use { outputStream ->
            outputStream.writer().use {
                it.write(spec.value ?: "")
            }
            return@let outputStream.toString(Charsets.UTF_8.name())
        }
    }

    private fun createInstance() = XmlPluginsSpecificationProvider(
        _pluginDescriptorsProvider,
        _xmlWriter,
        _loggerService,
        listOf(
            _folderPluginXmlElementGenerator,
            _filePluginXmlElementGenerator,
            _downloadPluginXmlElementGenerator
        )
    )
}