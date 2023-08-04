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

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.DocElement
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.*
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class XmlPluginsSpecificationProviderTest {
    @MockK
    private lateinit var _pluginParametersProvider: PluginParametersProvider

    @MockK
    private lateinit var _xmlWriter: XmlWriter

    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _folderPluginSource: PluginSource

    @MockK
    private lateinit var _filePluginSource: PluginSource

    @MockK
    private lateinit var _downloadPluginSource: PluginSource

    private val _folderElement = DocElement("Folder", "MyFolder")
    private val _fileElement = DocElement("File", "MyFile")
    private val _downloadElement = DocElement("Download", "MyPlugin/MyVersion")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _folderPluginSource.id } returns "folder"
        every { _folderPluginSource.getPlugin(any()) } returns DocElement("Folder")
        every { _folderPluginSource.getPlugin("folderSpec") } returns _folderElement

        every { _filePluginSource.id } returns "file"
        every { _filePluginSource.getPlugin(any()) } returns DocElement("File")
        every { _filePluginSource.getPlugin("fileSpec") } returns _fileElement

        every { _downloadPluginSource.id } returns "download"
        every { _downloadPluginSource.getPlugin(any()) } returns DocElement("Download")
        every { _downloadPluginSource.getPlugin("downloadSpec") } returns _downloadElement
    }

    @DataProvider
    fun getPluginSpecificationsTestData() = arrayOf(
        arrayOf(
            listOf(PluginParameter("", "")),
            null
        ),
        arrayOf(
            listOf(PluginParameter("   ", "   ")),
            null
        ),
        arrayOf(
            listOf(PluginParameter("unknownType", "unknownSpec")),
            null
        ),
        arrayOf(
            listOf(PluginParameter("Folder", "folderSpec")),
            DocElement("Packages", _folderElement)
        ),
        arrayOf(
            listOf(PluginParameter("folder", "folderSpec"), PluginParameter("file", "fileSpec"), PluginParameter("DownLoad", "downloadSpec")),
            DocElement("Packages", _folderElement, _fileElement, _downloadElement)
        ),
        arrayOf(
            listOf(PluginParameter("foLDER", "folderSpec"), PluginParameter("", ""), PluginParameter("file", "fileSpec")),
            DocElement("Packages", _folderElement, _fileElement)
        ),
        arrayOf(
            listOf(PluginParameter("abc", "folderSpec"), PluginParameter("filE", "fileSpec")),
            DocElement("Packages", _fileElement)
        ),
        arrayOf(
            listOf(PluginParameter("folder", "abc"), PluginParameter("file", "fileSpec")),
            DocElement("Packages", _fileElement)
        ),
        arrayOf(
            listOf(PluginParameter("folder", "abc"), PluginParameter("file", "fileSpec")),
            DocElement("Packages", _fileElement)
        ),
    )

    @Test(dataProvider = "getPluginSpecificationsTestData")
    fun `should provide proper plugin specifications in document format when has plugins`(
        pluginParameters: List<PluginParameter>,
        expectedPluginsSpecDocElement: DocElement?
    ) {
        // arrange
        val provider = createInstance()
        every { _loggerService.writeWarning(any()) } answers { }
        every { _pluginParametersProvider.getPluginParameters() } answers { pluginParameters }
        every { _xmlWriter.write(any(), any()) } answers {
            val element = arg<DocElement>(0)
            val stream = arg<OutputStream>(1)
            stream.writer().use {
                it.write(element.value ?: "")
            }
        }
        val expectedPluginsSpec: String? = serializeDocElement(expectedPluginsSpecDocElement)

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginParametersProvider.getPluginParameters() }
    }

    @Test
    fun `should emit warning when facing unrecognized plugin and correctly process others`() {
        // arrange
        val provider = createInstance()
        val pluginsParameters = listOf(
            PluginParameter("download", "downloadSpec"),
            PluginParameter("unknownType", "unknownValue"),
            PluginParameter("folder", "folderSpec")
        )
        val expectedPluginsSpecDocElement = DocElement("Packages", _downloadElement, _folderElement)
        every { _loggerService.writeWarning(any()) } answers { }
        every { _pluginParametersProvider.getPluginParameters() } answers { pluginsParameters }
        every { _xmlWriter.write(any(), any()) } answers {
            val element = arg<DocElement>(0)
            val stream = arg<OutputStream>(1)
            stream.writer().use {
                it.write(element.value ?: "")
            }
        }
        val expectedPluginsSpec: String? = serializeDocElement(expectedPluginsSpecDocElement)

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginParametersProvider.getPluginParameters() }
        verify(exactly = 1) { _xmlWriter.write(any(), any()) }
        verify(exactly = 1) { _loggerService.writeWarning(withArg { assertTrue(it.contains("unknownType")) }) }
    }

    private fun serializeDocElement(docElement: DocElement?) = docElement?.let { spec ->
        ByteArrayOutputStream().use { outputStream ->
            outputStream.writer().use {
                it.write(spec.value ?: "")
            }
            return@let outputStream.toString(Charsets.UTF_8.name())
        }
    }

    private fun createInstance(): PluginsSpecificationProvider {
        return XmlPluginsSpecificationProvider(
            _pluginParametersProvider,
            _xmlWriter,
            _loggerService,
            listOf(_folderPluginSource, _filePluginSource, _downloadPluginSource)
        )
    }
}