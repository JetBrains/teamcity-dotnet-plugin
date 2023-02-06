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

package jetbrains.buildServer.dotnet.test.dotnet.discovery.msbuild

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.discovery.msbuild.MSBuildValidatorImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildValidatorTest {
    @DataProvider
    fun testValidateData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPath"), "MSBuild.exe")),
                        File("msbuildPath"),
                        true
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("msbuildPath")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPathAbc"), "MSBuild.exe")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPath"), "MSBuild")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File(File("msbuildPath"), "MSBuild2.exe")),
                        File("msbuildPath"),
                        false
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        File("msbuildPath"),
                        false
                )
        )
    }


    @Test(dataProvider = "testValidateData")
    fun shouldValidate(fileSystemService: FileSystemService, msbuildPath: File, expectedResult: Boolean) {
        // Given
        val validator = createInstance(fileSystemService)

        // When
        val actualResult = validator.isValid(msbuildPath)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    private fun createInstance(fileSystemService: FileSystemService) = MSBuildValidatorImpl(fileSystemService)
}