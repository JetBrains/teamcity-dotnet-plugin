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

package jetbrains.buildServer

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ToolVersionProviderTest {
    @MockK private lateinit var _projectManager: ProjectManager
    @MockK private lateinit var _toolManager: ServerToolManager
    @MockK private lateinit var _toolType: ToolType
    @MockK private lateinit var _rootProject: SProject
    @MockK private lateinit var _toolVersion: ToolVersion

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _projectManager.rootProject } returns _rootProject
    }

    @Test
    fun shouldProvideToolVersion()
    {
        // Given
        val versionsProvider = createInstance()
        every { _toolManager.findToolType("ToolId") } returns _toolType
        every { _toolManager.resolveToolVersionReference(_toolType, "MyPath", _rootProject) } returns _toolVersion
        every { _toolVersion.version } returns "1.2.3"

        // When
        val actualVersion = versionsProvider.getVersion("MyPath", "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(1, 2, 3))
    }

    @Test
    fun shouldNotProvideToolVersionWhenCannotResolveReference()
    {
        // Given
        val versionsProvider = createInstance()
        every { _toolManager.findToolType("ToolId") } returns _toolType
        every { _toolManager.resolveToolVersionReference(_toolType, "MyPath", _rootProject) } returns null

        // When
        val actualVersion = versionsProvider.getVersion("MyPath", "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(0))
    }

    @Test
    fun shouldNotProvideToolVersionWhenCannotFindToolType()
    {
        // Given
        val versionsProvider = createInstance()
        every { _toolManager.findToolType("ToolId") } returns null

        // When
        val actualVersion = versionsProvider.getVersion("MyPath", "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(0))
    }

    @Test
    fun shouldNotProvideToolVersionWhenPathIsNull()
    {
        // Given
        val versionsProvider = createInstance()

        // When
        val actualVersion = versionsProvider.getVersion(null, "ToolId")

        // Then
        Assert.assertEquals(actualVersion, Version(0))
    }

    private fun createInstance() = ToolVersionProviderImpl(_projectManager, _toolManager)
}