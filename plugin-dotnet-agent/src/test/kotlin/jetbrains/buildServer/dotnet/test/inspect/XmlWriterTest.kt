package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.E
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.build
import jetbrains.buildServer.inspect.XmlWriterImpl
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import javax.xml.parsers.DocumentBuilderFactory

class XmlWriterTest {
    @Test
    fun shouldConvertToFlatSequency() {
        // Given
        val element = E("Abc", "Xyz")
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = docFactory.newDocumentBuilder()
        val doc = docBuilder.newDocument()

        val actualDoc = docBuilder.newDocument()
        actualDoc.build(element)

        val xmlDocumentService = mockk<XmlDocumentService>()
        every { xmlDocumentService.create() } returns doc
        every { xmlDocumentService.serialize(any(), any()) } returns Unit

        val xmlWriter = XmlWriterImpl(xmlDocumentService)
        ByteArrayOutputStream().use {
            // When
            xmlWriter.write(element, it)

            // Then
            verify { xmlDocumentService.create() }
            verify { xmlDocumentService.serialize(match { it.toString() == actualDoc.toString() }, it) }
        }
    }
}