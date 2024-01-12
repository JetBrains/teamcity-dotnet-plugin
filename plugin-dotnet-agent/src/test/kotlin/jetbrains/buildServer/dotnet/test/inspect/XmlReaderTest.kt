

package jetbrains.buildServer.dotnet.test.inspect

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.dotnet.test.agent.JsonParserTest
import jetbrains.buildServer.inspect.XmlReaderImpl
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File
import java.io.FileInputStream

class XmlReaderTest {
    @Test
    fun shouldConvertToFlatSequency() {
        // Given
        val xmlReader = XmlReaderImpl()
        var xmlFile = File(JsonParserTest::class.java.classLoader.getResource("sample.xml")!!.file)

        // When
        var actualResult = FileInputStream(xmlFile).use { xmlReader.read(it).toList() }

        // Then
        Assert.assertEquals(
            actualResult,
            listOf(
                XmlElement("Report").withAttribute("ToolsVersion", "203"),
                XmlElement("Information"),
                XmlElement("InspectionScope"),
                XmlElement("Element", "Solution"),
                XmlElement("IssueTypes"),
                XmlElement("IssueType").withAttribute("Id", "Arrange"),
                XmlElement("IssueType").withAttribute("Severity", "ERROR"),
                XmlElement("Text", "my text").withAttribute("atr", "val"),
                XmlElement("Issues"),
                XmlElement("Project").withAttribute("Name", "Clock"),
                XmlElement("Issue").withAttribute("TypeId", "Redundant")
            )
        )
    }
}