

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.build
import org.w3c.dom.Document
import java.io.OutputStream

class XmlWriterImpl(
    private val _xmlDocumentService: XmlDocumentService
) : XmlWriter {
    override fun write(rootElement: XmlElement, xmlStream: OutputStream) {
        val doc: Document = _xmlDocumentService.create()
        doc.build(rootElement)
        _xmlDocumentService.serialize(doc, xmlStream)
    }
}