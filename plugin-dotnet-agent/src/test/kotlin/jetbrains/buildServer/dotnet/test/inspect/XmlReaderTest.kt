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