package jetbrains.buildServer.runners

import org.w3c.dom.Document
import java.io.InputStream
import java.io.OutputStream

interface XmlDocumentService: Serializer<Document>, Deserializer<Document> {
    fun create(): Document;
}