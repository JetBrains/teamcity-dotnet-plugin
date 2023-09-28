/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.XmlElement
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
        val element = XmlElement("Abc", "Xyz")
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