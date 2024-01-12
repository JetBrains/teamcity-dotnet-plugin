

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.DownloadPluginXmlElementGenerator
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DownloadPluginXmlElementGeneratorTest {
    @MockK
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _loggerService.writeWarning(any()) } answers { }
    }

    @DataProvider
    fun getCorrectSpecificationAndResults() = arrayOf(
        arrayOf("StyleCop.StyleCop/2021.2.1", XmlElement("Download").withAttribute("Id", "StyleCop.StyleCop").withAttribute("Version", "2021.2.1")),
        arrayOf("StyleCop.StyleCop/2021.2.1-eap", XmlElement("Download").withAttribute("Id", "StyleCop.StyleCop").withAttribute("Version", "2021.2.1-eap")),
    )

    @Test(dataProvider = "getCorrectSpecificationAndResults")
    fun `should generate non empty element and emit no warnings when specification is correct`(specification: String, expectedXmlElement: XmlElement) {
        // arrange
        val source = createInstance()

        // act
        val actualXmlElement = source.generateXmlElement(specification)

        // assert
        Assert.assertEquals(actualXmlElement, expectedXmlElement)
        verify(exactly = 0) { _loggerService.writeWarning(any()) }
    }

    @DataProvider
    fun getIncorrectSpecifications() = arrayOf(
        arrayOf("StyleCop.StyleCop"),
        arrayOf("C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3.nupkg"),
        arrayOf("home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3.nupkg"),
        arrayOf(""),
        arrayOf("  ")
    )

    @Test(dataProvider = "getIncorrectSpecifications")
    fun `should generate empty element and emit warning when specification is incorrect`(specification: String) {
        // arrange
        val generator = createInstance()

        // act
        val actualXmlElement = generator.generateXmlElement(specification)

        // assert
        Assert.assertEquals(actualXmlElement, XmlElement("Download"))
        verify(exactly = 1) { _loggerService.writeWarning(withArg { Assert.assertTrue(it.contains("it will be ignored")) }) }
    }

    private fun createInstance() = DownloadPluginXmlElementGenerator(_loggerService)
}