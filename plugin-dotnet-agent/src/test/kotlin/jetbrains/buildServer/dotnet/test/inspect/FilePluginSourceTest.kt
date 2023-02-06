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
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.inspect.FilePluginSource
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class FilePluginSourceTest {
    @DataProvider(name = "getPluginCases")
    fun getPluginCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService().addFile(File("MyFile")),
                        E("File").a("Path", File("MyFile").canonicalFile.absolutePath)
                ),
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService().addFile(File("MyFile2")),
                        E("File")
                ),
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService().addDirectory(File("MyFile")),
                        E("File")
                ),
                arrayOf(
                        "MyFile",
                        VirtualFileSystemService(),
                        E("File")
                )
        )
    }

    @Test(dataProvider = "getPluginCases")
    fun shouldGetPlugin(specification: String, fileSystem: FileSystemService, expectedPlugin: E) {
        // Given
        val source = createInstance(fileSystem)

        // When
        val aclualPlugin = source.getPlugin(specification)

        // Then
        Assert.assertEquals(aclualPlugin, expectedPlugin)
    }

    private fun createInstance(fileSystem: FileSystemService) =
            FilePluginSource(fileSystem)
}