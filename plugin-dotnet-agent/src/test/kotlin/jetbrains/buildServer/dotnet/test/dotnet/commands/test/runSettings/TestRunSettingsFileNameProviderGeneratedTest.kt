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

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.runSettings

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.Serializer
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsFileNameProviderGenerated
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.w3c.dom.Document
import java.io.File
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory

class TestRunSettingsFileNameProviderGeneratedTest {
    private val _documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    private val _settings = _documentBuilder.newDocument()
    @MockK private lateinit var _settingsProvider: TestRunSettingsProvider
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _fileSystem: FileSystemService
    @MockK private lateinit var _serializer: Serializer<Document>

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    public fun shouldGenerateSettingsFile() {
        // Given
        val provider = createInstance()
        val settingsFile = File("Abc.settings")
        val outputStream = mockk<OutputStream>()

        // When
        every { _settingsProvider.tryCreate(DotnetCommandType.Test) } returns _settings
        every { _pathsService.getTempFileName(TestRunSettingsFileNameProviderGenerated.RunSettingsFileExtension) } returns settingsFile
        every { _fileSystem.write<File>(settingsFile, any()) } answers {
            arg<(OutputStream) -> File>(1).invoke(outputStream)
            settingsFile
        }
        every { _serializer.serialize(_settings, outputStream) } returns Unit
        val actualFile = provider.tryGet(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFile, settingsFile)
        verify { _serializer.serialize(_settings, outputStream) }
    }

    @Test
    public fun shouldNotGenerateSettingsFileWhenHasNoSettings() {
        // Given
        val provider = createInstance()

        // When
        every { _settingsProvider.tryCreate(DotnetCommandType.Test) } returns null
        val actualFile = provider.tryGet(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFile, null)
        verify(exactly = 0) { _pathsService.getTempFileName(TestRunSettingsFileNameProviderGenerated.RunSettingsFileExtension) }
    }

    @Test
    public fun shouldNotGenerateSettingsFileWhenErrorOnCreatingSettings() {
        // Given
        val provider = createInstance()

        // When
        every { _settingsProvider.tryCreate(DotnetCommandType.Test) } throws Throwable()
        val actualFile = provider.tryGet(DotnetCommandType.Test)

        // Then
        Assert.assertEquals(actualFile, null)
    }

    private fun createInstance() = TestRunSettingsFileNameProviderGenerated(_settingsProvider, _pathsService, _fileSystem, _serializer)
}