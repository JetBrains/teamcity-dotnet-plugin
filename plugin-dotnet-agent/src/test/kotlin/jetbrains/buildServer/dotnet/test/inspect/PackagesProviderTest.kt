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

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.E
import jetbrains.buildServer.inspect.PackagesProviderImpl
import jetbrains.buildServer.inspect.PluginSource
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PackagesProviderTest {
    @MockK private val _folderPluginSource: PluginSource = mockk<PluginSource>()
    @MockK private val _filePluginSource: PluginSource = mockk<PluginSource>()
    private val _folderElement = E("Folder", "MyFolder")
    private val _fileElement = E("File", "MyFile")

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { _folderPluginSource.id } returns "Folder"
        every { _folderPluginSource.getPlugin(any()) } returns E("Folder" )
        every { _folderPluginSource.getPlugin("folderSpec") } returns _folderElement

        every { _filePluginSource.id } returns "filE"
        every { _filePluginSource.getPlugin(any()) } returns E("File" )
        every { _filePluginSource.getPlugin("fileSpec") } returns _fileElement
    }

    @DataProvider(name = "getPackagesCases")
    fun getPackagesCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "", E("Packages")),
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "   ", E("Packages")),
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "folder folderSpec", E("Packages", _folderElement)),
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "folder folderSpec\nfile fileSpec", E("Packages", _folderElement, _fileElement)),
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "\n folder   folderSpec  \n  \nfile fileSpec", E("Packages", _folderElement, _fileElement)),
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "abc folderSpec\nfile fileSpec", E("Packages", _fileElement)),
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "folder abc\nfile fileSpec", E("Packages", _fileElement)),
                arrayOf(listOf(_folderPluginSource, _filePluginSource), "folderfolderSpec\nfile fileSpec", E("Packages", _fileElement))
        )
    }

    @Test(dataProvider = "getPackagesCases")
    fun shouldGetPackages(
            sources: List<PluginSource>,
            specification: String,
            expectedPackages: E) {
        // Given
        val provider = createInstance(sources)

        // When
        val actualPackages = provider.getPackages(specification)

        // Then
        Assert.assertEquals(actualPackages.toString(), expectedPackages.toString())
    }

    private fun createInstance(sources: List<PluginSource>) =
            PackagesProviderImpl(sources)
}