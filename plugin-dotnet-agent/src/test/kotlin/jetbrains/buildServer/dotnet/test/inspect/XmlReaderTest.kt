package jetbrains.buildServer.dotnet.test.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.dotnet.test.agent.JsonParserTest
import jetbrains.buildServer.inspect.XmlReaderImpl
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File
import java.io.FileInputStream
import javax.xml.stream.XMLInputFactory

class XmlReaderTest {
    @Test
    fun shouldConvertToFlatSequency() {
        // Given
        var actualResult: List<E>? = null
        val xmlReader = XmlReaderImpl()
        var xmlFile = File(JsonParserTest::class.java.classLoader.getResource("sample.xml")!!.file)
        FileInputStream(xmlFile).use {
            val xmlInFact = XMLInputFactory.newInstance()
            val reader = xmlInFact.createXMLStreamReader(it)
            try {
                // When
                actualResult = xmlReader.read(it).toList()
            } finally {
                reader.close()
            }
        }

        // Then
        Assert.assertEquals(
                actualResult,
                listOf(
                        E("Report").a("ToolsVersion", "203"),
                        E("Information"),
                        E("InspectionScope"),
                        E("Element", "Solution"),
                        E("IssueTypes"),
                        E("IssueType").a("Id", "Arrange"),
                        E("IssueType").a("Severity", "ERROR"),
                        E("Text", "my text").a("atr", "val"),
                        E("Issues"),
                        E("Project").a("Name", "Clock"),
                        E("Issue").a("TypeId", "Redundant")
                )
        )
    }
}