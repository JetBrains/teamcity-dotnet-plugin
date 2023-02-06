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
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.discovery.VisualStudioTestAgentPropertiesProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class VisualStudioTestAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioTestInstanceProvider: ToolInstanceProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideAgentProperties() {
        // Given
        val propertiesProvider = createInstance()

        // When
        every { _visualStudioTestInstanceProvider.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.VisualStudio, File("path1"), Version(16, 0, 18362), Version(16, 0), Platform.Default),
                ToolInstance(ToolInstanceType.VisualStudioTest, File("path"), Version(16, 0, 18362), Version(16, 5, 4), Platform.Default),
                ToolInstance(ToolInstanceType.MSTest, File("path2"), Version(16, 0, 18362), Version(16, 2,1), Platform.Default))

        // Then
        Assert.assertEquals(propertiesProvider.desription, "Visual Studio Test Console")
        Assert.assertEquals(
                propertiesProvider.properties.toList(),
                listOf(AgentProperty(ToolInstanceType.VisualStudioTest, "teamcity.dotnet.vstest.16.5", "path"))
        )
    }

    private fun createInstance() =
            VisualStudioTestAgentPropertiesProvider(_visualStudioTestInstanceProvider)
}