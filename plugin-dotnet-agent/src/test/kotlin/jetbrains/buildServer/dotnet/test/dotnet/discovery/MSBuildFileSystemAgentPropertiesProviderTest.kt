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

package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.discovery.MSBuildFileSystemAgentPropertiesProvider
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildFileSystemAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioLocator: ToolInstanceProvider
    @MockK private lateinit var _peReader: PEReader

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testProperties(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path),
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild22.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/1abc6.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        emptyList<ToolInstance>(),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.MSTest, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudioTest, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        emptyList<AgentProperty>()
                )
        )
    }

    @Test(dataProvider = "testProperties")
    fun shouldProvideProperties(
            fileSystemService: FileSystemService,
            visualStudioInstances: List<ToolInstance>,
            expectedProperties: List<AgentProperty>) {
        // Given
        val propertiesProvider = createInstance(fileSystemService)
        every { _visualStudioLocator.getInstances() } returns visualStudioInstances
        every { _peReader.tryGetVersion(any()) } returns Version(16, 0, 0, 0)

        // When
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(actualProperties, expectedProperties)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            MSBuildFileSystemAgentPropertiesProvider(listOf(_visualStudioLocator), fileSystemService, _peReader)
}