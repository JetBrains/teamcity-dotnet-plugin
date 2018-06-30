package jetbrains.buildServer

import org.w3c.dom.Document

interface XmlDocumentService : Serializer<Document>, Deserializer<Document> {
    fun create(): Document
}