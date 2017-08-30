package jetbrains.buildServer.runners

import jetbrains.buildServer.RunBuildException
import org.w3c.dom.Document
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class XmlDocumentServiceImpl : XmlDocumentService{
    override fun create(): Document {
        val docFactory = DocumentBuilderFactory.newInstance()
        val docBuilder: DocumentBuilder
        try {
            docBuilder = docFactory.newDocumentBuilder()
        } catch (ex: ParserConfigurationException) {
            throw RunBuildException("Error during creating xml document")
        }

        return docBuilder.newDocument()
    }

    override fun deserialize(inputStream: InputStream): Document {
        val factory = DocumentBuilderFactory.newInstance()
        try {
            val builder = factory.newDocumentBuilder()
            return builder.parse(inputStream)
        } catch (ex: Exception) {
            throw RunBuildException("Error during parsing the xml document from text")
        }
    }

    override fun serialize(document: Document, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        val result = StreamResult(writer)
        val transformerFactory = TransformerFactory.newInstance()
        try {
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val source = DOMSource(document)
            transformer.transform(source, result)
        } catch (ex: TransformerException) {
            throw RunBuildException("Error during converting the xml document to text")
        }
    }

}