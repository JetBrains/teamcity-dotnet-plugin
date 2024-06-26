

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.inspect.FilePluginXmlElementGenerator
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class FilePluginXmlElementGeneratorTest {
    @MockK
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _loggerService.writeWarning(any()) } answers { }
    }

    @Test
    fun `should generate non empty element and emit no warnings when specification is correct`() {
        // arrange
        val specification = "PluginFile"
        val fileSystem = VirtualFileSystemService().addFile(File("PluginFile"))
        val expectedPlugin = XmlElement("File").withAttribute("Path", File("PluginFile").canonicalFile.absolutePath)
        val source = createInstance(fileSystem)

        // act
        val actualPlugin = source.generateXmlElement(specification)

        // assert
        Assert.assertEquals(actualPlugin, expectedPlugin)
        verify(exactly = 0) { _loggerService.writeWarning(any()) }
    }

    @DataProvider
    fun getIncorrectSpecifications() = arrayOf(
        arrayOf(
            "PluginFile",
            VirtualFileSystemService().addFile(File("PluginFile2"))
        ),
        arrayOf(
            "PluginFile",
            VirtualFileSystemService().addDirectory(File("PluginFile"))
        ),
        arrayOf(
            "PluginFile",
            VirtualFileSystemService()
        )
    )

    @Test(dataProvider = "getIncorrectSpecifications")
    fun `should generate empty element and emit warning when specification is incorrect`(specification: String, fileSystem: FileSystemService) {
        // arrange
        val source = createInstance(fileSystem)

        // act
        val actualPlugin = source.generateXmlElement(specification)

        // assert
        Assert.assertEquals(actualPlugin, XmlElement("File"))
        verify(exactly = 1) { _loggerService.writeWarning(withArg { Assert.assertTrue(it.contains("it will be ignored")) }) }
    }

    private fun createInstance(fileSystem: FileSystemService) = FilePluginXmlElementGenerator(fileSystem, _loggerService)
}