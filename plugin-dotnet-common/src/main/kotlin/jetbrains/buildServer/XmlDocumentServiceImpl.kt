/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer

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

class XmlDocumentServiceImpl : XmlDocumentService {
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

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
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