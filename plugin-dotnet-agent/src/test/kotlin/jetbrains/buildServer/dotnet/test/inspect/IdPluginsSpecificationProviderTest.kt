package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.inspect.PluginDescriptorType.ID
import jetbrains.buildServer.inspect.PluginDescriptorType.SOURCE
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class IdPluginsSpecificationProviderTest {
    @MockK
    private lateinit var _pluginDescriptorsProvider: PluginDescriptorsProvider

    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _folderPluginXmlElementGenerator: PluginXmlElementGenerator

    @MockK
    private lateinit var _filePluginXmlElementGenerator: PluginXmlElementGenerator

    @MockK
    private lateinit var _downloadPluginXmlElementGenerator: PluginXmlElementGenerator

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _loggerService.writeWarning(any()) } answers { }

        every { _folderPluginXmlElementGenerator.sourceId } returns "folder"
        every { _filePluginXmlElementGenerator.sourceId } returns "file"
        every { _downloadPluginXmlElementGenerator.sourceId } returns "download"
    }

    @DataProvider
    fun getCorrectPluginDescriptorsAndResults() = arrayOf(
        arrayOf(listOf(PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1")), "StyleCop.StyleCop/2021.2.1"),
        arrayOf(listOf(PluginDescriptor(ID, "StyleCop.StyleCop")), "StyleCop.StyleCop"),
        arrayOf(listOf(PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"), PluginDescriptor(ID, "Unity.Unity/2020.0")), "StyleCop.StyleCop/2021.2.1;Unity.Unity/2020.0"),
        arrayOf(listOf(PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"), PluginDescriptor(ID, "Unity.Unity")), "StyleCop.StyleCop/2021.2.1;Unity.Unity"),
        arrayOf(listOf(PluginDescriptor(ID, "StyleCop.StyleCop"), PluginDescriptor(ID, "Unity.Unity")), "StyleCop.StyleCop;Unity.Unity"),
    )

    @Test(dataProvider = "getCorrectPluginDescriptorsAndResults")
    fun `should create correct plugins specification in semicolon-separated format when having correct plugin descriptors`(
        pluginDescriptors: List<PluginDescriptor>,
        expectedPluginsSpec: String?
    ) {
        // arrange
        val provider = createInstance()
        every { _pluginDescriptorsProvider.getPluginDescriptors() } answers { pluginDescriptors }

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginDescriptorsProvider.getPluginDescriptors() }
        verify(exactly = 0) { _loggerService.writeWarning(any()) }
    }

    @DataProvider
    fun getIncorrectPluginDescriptors() = arrayOf(
        arrayOf(listOf(PluginDescriptor(SOURCE, "StyleCop.StyleCop/2021.2.1"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "StyleCop.StyleCop"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "someSource someValue"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "Download StyleCop.StyleCop/2021.2.1"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "File C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3.nupkg"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "Folder home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3"))),
        arrayOf(listOf(PluginDescriptor(SOURCE, "StyleCop.StyleCop/2021.2.1"), PluginDescriptor(SOURCE, "StyleCop.StyleCop"))),
    )

    @Test(dataProvider = "getIncorrectPluginDescriptors")
    fun `should emit warnings and not create specification when facing invalid plugin descriptors`(pluginDescriptors: List<PluginDescriptor>) {
        // arrange
        val provider = createInstance()
        every { _pluginDescriptorsProvider.getPluginDescriptors() } answers { pluginDescriptors }

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertNull(actualPluginsSpec)
        verify(exactly = 1) { _pluginDescriptorsProvider.getPluginDescriptors() }
        verify(exactly = pluginDescriptors.size) { _loggerService.writeWarning(withArg { assertTrue(it.contains("it will be ignored")) }) }
    }

    @DataProvider
    fun getPartiallyCorrectPluginDescriptorsAndResults() = arrayOf(
        arrayOf(
            listOf(
                PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"),
                PluginDescriptor(SOURCE, "Download StyleCop.StyleCop/2021.2.1")
            ),
            "StyleCop.StyleCop/2021.2.1"
        ),
        arrayOf(
            listOf(
                PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"),
                PluginDescriptor(SOURCE, "File C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3.nupkg")
            ),
            "StyleCop.StyleCop/2021.2.1"
        ),
        arrayOf(
            listOf(
                PluginDescriptor(ID, "StyleCop.StyleCop/2021.2.1"),
                PluginDescriptor(SOURCE, "Folder home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3")
            ),
            "StyleCop.StyleCop/2021.2.1"
        ),
    )

    @Test(dataProvider = "getPartiallyCorrectPluginDescriptorsAndResults")
    fun `should create partial specification and emit warnings when facing both invalid and valid plugin descriptors`(
        pluginDescriptors: List<PluginDescriptor>,
        expectedPluginsSpec: String
    ) {
        // arrange
        val provider = createInstance()
        every { _pluginDescriptorsProvider.getPluginDescriptors() } answers { pluginDescriptors }

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginDescriptorsProvider.getPluginDescriptors() }
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
        verify(exactly = 0) { _loggerService.writeWarning(withArg { any() }) }
    }

    private fun createInstance(): PluginsSpecificationProvider {
        return IdPluginsSpecificationProvider(
            _pluginDescriptorsProvider,
            _loggerService,
            listOf(_folderPluginXmlElementGenerator, _filePluginXmlElementGenerator, _downloadPluginXmlElementGenerator)
        )
    }
}