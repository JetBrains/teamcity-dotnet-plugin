/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.dotnet.DotnetSdk
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetRuntime
import jetbrains.buildServer.dotnet.DotnetRuntimesProviderImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetRuntimesProviderTest {

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"))
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3-rc"))
                                .addFile(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.4"))
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "nuget"))
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.5")),
                        listOf(
                                DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"), Version(1, 2, 3), "NETCore"),
                                DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3-rc"), Version.parse("1.2.3-rc"), "NETCore"),
                                DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.5"), Version(1, 2, 5), "NETCore")
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"))
                                .addFile(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.4")),
                        listOf(DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"), Version(1, 2, 3), "NETCore"))
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("dotnet", "shared"), "Microsoft.NETCore.App")),
                        emptyList<DotnetSdk>()
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideRuntimes(fileSystemService: FileSystemService, expectedRuntimes: List<DotnetRuntime>) {
        // Given
        val toolPath = File("dotnet", "dotnet.exe")
        val provider = createInstance(fileSystemService)

        // When
        val actualRuntimes = provider.getRuntimes(toolPath).toList()

        // Then
        Assert.assertEquals(actualRuntimes, expectedRuntimes)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetRuntimesProviderImpl(fileSystemService)

    companion object {
        private val runtimesPath = File(File(File("Program Files"), "dotnet"), "shared")
    }
}