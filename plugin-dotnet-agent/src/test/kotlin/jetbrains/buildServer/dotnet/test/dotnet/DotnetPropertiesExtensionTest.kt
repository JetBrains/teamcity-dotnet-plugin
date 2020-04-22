/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.subjectOf
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetPropertiesExtensionTest {
    @MockK private lateinit var _eventSources: EventSources
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _dotnetVersionProvider: DotnetVersionProvider
    @MockK private lateinit var _dotnetSdksProvider: DotnetSdksProvider
    @MockK private lateinit var _toolProvider: ToolProvider
    @MockK private lateinit var _buildAgent: BuildAgent
    @MockK private lateinit var _buildAgentConfiguration: BuildAgentConfiguration

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<Sequence<Any>>> {
        return arrayOf(
                arrayOf(
                        emptySequence(),
                        emptySequence()),

                arrayOf(
                        sequenceOf(
                                DotnetSdk(File("1.0.0"), Version(1, 0, 0))),
                        sequenceOf(
                                "1.0" to File("1.0.0").absolutePath,
                                "1.0.0" to File("1.0.0").absolutePath)),

                // Select newest version as default for group by Version(x, y)
                arrayOf(
                        sequenceOf(
                                DotnetSdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetSdk(File("1.1.300"), Version(1, 1, 300)),
                                DotnetSdk(File("1.1.1"), Version(1, 1, 1))),
                        sequenceOf(
                                "1.1" to File("1.1.300").absolutePath,
                                "1.1.1" to File("1.1.1").absolutePath,
                                "1.1.100" to File("1.1.100").absolutePath,
                                "1.1.300" to File("1.1.300").absolutePath)),

                // Display preview versions
                arrayOf(
                        sequenceOf(
                                DotnetSdk(File("1.1.100"), Version(1, 1, 100)),
                                DotnetSdk(File("1.1.300-preview"), Version.parse("1.1.300-preview"))),
                        sequenceOf(
                                "1.1" to File("1.1.300-preview").absolutePath,
                                "1.1.100" to File("1.1.100").absolutePath,
                                "1.1.300-preview" to File("1.1.300-preview").absolutePath))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideConfigParams(
            originSdks: Sequence<DotnetSdk>,
            expectedSdks: Sequence<Pair<String, String>>) {
        // Given
        val toolPath = Path("dotnet")
        val workPath = Path("work")

        val beforeAgentConfigurationLoadedSource = subjectOf<EventSources.BeforeAgentConfigurationLoaded>()
        every { _eventSources.beforeAgentConfigurationLoadedSource } returns beforeAgentConfigurationLoadedSource
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns toolPath.path
        every { _pathsService.getPath(PathType.Work) } returns File(workPath.path)
        every { _dotnetVersionProvider.getVersion(toolPath, workPath) } returns Version(1, 0, 1)
        every { _dotnetSdksProvider.getSdks(File(toolPath.path)) } returns originSdks
        every { _buildAgent.configuration } returns _buildAgentConfiguration
        every { _buildAgentConfiguration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, Version(1, 0, 1).toString()) } returns Unit
        every { _buildAgentConfiguration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, File(toolPath.path).canonicalPath) } returns Unit

        for ((version, path) in expectedSdks) {
            every { _buildAgentConfiguration.addConfigurationParameter("${DotnetConstants.CONFIG_SDK_NAME}$version${DotnetConstants.PATH_SUFFIX}", path) } returns Unit
        }

        createInstance()

        // When
        beforeAgentConfigurationLoadedSource.onNext(EventSources.BeforeAgentConfigurationLoaded(_buildAgent))

        // Then
        verify { _buildAgentConfiguration.addConfigurationParameter(DotnetConstants.CONFIG_NAME, Version(1, 0, 1).toString()) }
        verify { _buildAgentConfiguration.addConfigurationParameter(DotnetConstants.CONFIG_PATH, File(toolPath.path).canonicalPath) }

        for ((version, path) in expectedSdks) {
            verify { _buildAgentConfiguration.addConfigurationParameter("${DotnetConstants.CONFIG_SDK_NAME}$version${DotnetConstants.PATH_SUFFIX}", path) }
        }
    }

    private fun createInstance(): DotnetPropertiesExtension {
        val propertiesExtension = DotnetPropertiesExtension(
                _toolProvider,
                _dotnetVersionProvider,
                _dotnetSdksProvider,
                _pathsService)

        propertiesExtension.subscribe(_eventSources)
        return propertiesExtension
    }
}