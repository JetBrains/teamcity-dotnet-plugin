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

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetSdk
import jetbrains.buildServer.dotnet.DotnetSdksProviderImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetSdksProviderTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.3"))
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.3-rc"))
                                .addFile(File(File("dotnet", "sdk"), "1.2.4"))
                                .addDirectory(File(File("dotnet", "sdk"), "nuget"))
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.5")),
                        listOf(
                                DotnetSdk(File(File("dotnet", "sdk"), "1.2.3"), Version(1, 2, 3)),
                                DotnetSdk(File(File("dotnet", "sdk"), "1.2.3-rc"), Version.parse("1.2.3-rc")),
                                DotnetSdk(File(File("dotnet", "sdk"), "1.2.5"), Version(1, 2, 5))
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.3"))
                                .addFile(File(File("dotnet", "sdk"), "1.2.4")),
                        listOf(DotnetSdk(File(File("dotnet", "sdk"), "1.2.3"), Version(1, 2, 3)))
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File("dotnet", "sdk")),
                        emptyList<DotnetSdk>()
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideSdks(fileSystemService: FileSystemService, expectedSdks: List<DotnetSdk>) {
        // Given
        val toolPath = File("dotnet", "dotnet.exe")
        val provider = createInstance(fileSystemService)

        // When
        val actualSdks = provider.getSdks(toolPath).toList()

        // Then
        Assert.assertEquals(actualSdks, expectedSdks)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetSdksProviderImpl(fileSystemService)

    companion object {
        private val sdksPath = File(File(File("Program Files"), "dotnet"), "sdk")
    }
}