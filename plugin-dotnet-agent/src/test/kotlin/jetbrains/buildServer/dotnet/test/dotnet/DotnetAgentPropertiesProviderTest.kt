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

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetAgentPropertiesProviderTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _dotnetVersionProvider: DotnetVersionProvider
    @MockK private lateinit var _dotnetSdksProvider: DotnetSdksProvider
    @MockK private lateinit var _toolProvider: ToolProvider
    private val _toolPath = Path("dotnet")
    private val _workPath = Path("work")
    private val _configPathProperty = AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_PATH, File(_toolPath.path).canonicalPath)
    private val _configNameProperty = AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_NAME, "1.0.1")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<List<Any>>> {
        return arrayOf(
                arrayOf(
                        emptyList<DotnetSdk>(),
                        listOf(_configPathProperty, _configNameProperty)),

                arrayOf(
                        listOf(
                                DotnetSdk(File("1.0.0"), Version(1, 0, 0))),
                        listOf(
                                _configPathProperty, _configNameProperty,
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.0${DotnetConstants.PATH_SUFFIX}", File("1.0.0").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.0.0${DotnetConstants.PATH_SUFFIX}", File("1.0.0").absolutePath))),

                // Select newest version as default for group by Version(x, y)
                arrayOf(
                        listOf(
                                DotnetSdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetSdk(File("1.1.300"), Version(1, 1, 300)),
                                DotnetSdk(File("1.1.1"), Version(1, 1, 1))),
                        listOf(
                                _configPathProperty, _configNameProperty,
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.1${DotnetConstants.PATH_SUFFIX}", File("1.1.300").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.1.100${DotnetConstants.PATH_SUFFIX}", File("1.1.100").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.1.300${DotnetConstants.PATH_SUFFIX}", File("1.1.300").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.1.1${DotnetConstants.PATH_SUFFIX}", File("1.1.1").absolutePath))),

                // Display preview versions
                arrayOf(
                        listOf(
                                DotnetSdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetSdk(File("1.1.300-preview"), Version.parse("1.1.300-preview"))),
                        listOf(
                                _configPathProperty, _configNameProperty,
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.1${DotnetConstants.PATH_SUFFIX}", File("1.1.300-preview").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.1.100${DotnetConstants.PATH_SUFFIX}", File("1.1.100").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_SDK_NAME}1.1.300-preview${DotnetConstants.PATH_SUFFIX}", File("1.1.300-preview").absolutePath)))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideAgentProperties(
            originSdks: List<DotnetSdk>,
            expectedProperties: List<AgentProperty>) {
        // Given
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns _toolPath.path
        every { _pathsService.getPath(PathType.Work) } returns File(_workPath.path)
        every { _dotnetVersionProvider.getVersion(_toolPath, _workPath) } returns Version(1, 0, 1)
        every { _dotnetSdksProvider.getSdks(File(_toolPath.path)) } returns originSdks.asSequence()

        val propertiesProvider = createInstance()

        // When
        Assert.assertEquals(propertiesProvider.desription, ".NET CLI")
        Assert.assertEquals(propertiesProvider.properties.toList(), expectedProperties)
    }

    private fun createInstance() = DotnetAgentPropertiesProvider(
                _toolProvider,
                _dotnetVersionProvider,
                _dotnetSdksProvider,
                _pathsService)
}