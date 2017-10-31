package jetbrains.buildServer

import jetbrains.buildServer.Deserializer
import jetbrains.buildServer.Serializer
import org.w3c.dom.Document

interface XmlDocumentService: Serializer<Document>, Deserializer<Document> {
    fun create(): Document
}