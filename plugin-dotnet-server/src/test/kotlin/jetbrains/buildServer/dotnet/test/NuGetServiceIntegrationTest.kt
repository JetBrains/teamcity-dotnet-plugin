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

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.HttpDownloaderImpl
import jetbrains.buildServer.NuGetServiceImpl
import org.testng.Assert
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream

class NuGetServiceIntegrationTest {
    @Test
    fun shouldGetPackages() {
        // Given
        val downloader = HttpDownloaderImpl()
        val nuget = NuGetServiceImpl(downloader)

        // When
        val packages = nuget.getPackagesById("IoC.Container").toList()
        val downloadUrl = packages.last().downloadUrl
        var data: ByteArray?
        ByteArrayOutputStream().use { stream ->
            downloader.download(downloadUrl, stream)
            data = stream.toByteArray()
        }

        // Then
        Assert.assertTrue(packages.size >= 3)
        Assert.assertTrue(data!!.size >= 10000)
    }
}