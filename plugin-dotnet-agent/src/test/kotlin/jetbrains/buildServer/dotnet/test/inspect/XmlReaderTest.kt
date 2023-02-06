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

import jetbrains.buildServer.E
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