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

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.inspect.FolderPluginXmlElementGenerator
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class FolderPluginXmlElementGeneratorTest {
    @MockK
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _loggerService.writeWarning(any()) } answers { }
    }

    @Test
    fun `should generate non empty element and emit no warnings when specification is correct`() {
        // arrange
        val specification = "MyPluginFolder"
        val fileSystem = VirtualFileSystemService().addDirectory(File("MyPluginFolder"))
        val expectedPlugin = XmlElement("Folder").withAttribute("Path", File("MyPluginFolder").canonicalFile.absolutePath)
        val source = createInstance(fileSystem)

        // act
        val actualPlugin = source.generateXmlElement(specification)

        // assert
        Assert.assertEquals(actualPlugin, expectedPlugin)
        verify(exactly = 0) { _loggerService.writeWarning(any()) }
    }

    @DataProvider
    fun getIncorrectSpecifications() = arrayOf(
        arrayOf(
            "MyPluginFolder",
            VirtualFileSystemService().addDirectory(File("MyPluginFolder2"))
        ),
        arrayOf(
            "MyPluginFolder",
            VirtualFileSystemService().addFile(File("MyPluginFolder"))
        ),
        arrayOf(
            "MyPluginFolder",
            VirtualFileSystemService()
        )
    )

    @Test(dataProvider = "getIncorrectSpecifications")
    fun `should generate empty element and emit warning when specification is incorrect`(specification: String, fileSystem: FileSystemService) {
        // arrange
        val source = createInstance(fileSystem)

        // act
        val actualPlugin = source.generateXmlElement(specification)

        // assert
        Assert.assertEquals(actualPlugin, XmlElement("Folder"))
        verify(exactly = 1) { _loggerService.writeWarning(withArg { Assert.assertTrue(it.contains("it will be ignored")) }) }
    }

    private fun createInstance(fileSystem: FileSystemService) = FolderPluginXmlElementGenerator(fileSystem, _loggerService)
}