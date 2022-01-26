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

package jetbrains.buildServer.dotnet.test.mono

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.mono.MonoToolProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MonoToolProviderTest {
    @MockK private lateinit var _toolProvidersRegistry: ToolProvidersRegistry
    @MockK private lateinit var _toolSearchService: ToolSearchService
    @MockK private lateinit var _toolEnvironment: ToolEnvironment
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun supportToolCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("mono", true),
                arrayOf("MoNo", true),
                arrayOf("MONO", true),
                arrayOf("MONO2", false),
                arrayOf("abc", false),
                arrayOf(" mono ", false),
                arrayOf("   ", false),
                arrayOf("", false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldSupportTool(toolName: String, expectedResult: Boolean) {
        // Given
        every { _toolProvidersRegistry.registerToolProvider(any()) } returns Unit
        val toolProvider = createInstance()

        // When
        val actualResult = toolProvider.supports(toolName)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }


    @Test
    fun shouldGetPath() {
        // Given
        every { _toolProvidersRegistry.registerToolProvider(any()) } returns Unit

        val toolProvider = createInstance()
        val homePaths = sequenceOf(Path("a"))
        val defaultPaths = sequenceOf(Path("b"))
        val environmentPaths = sequenceOf(Path("c"))
        val paths = mutableListOf<Sequence<Path>>()

        // When
        every { _toolEnvironment.homePaths } returns homePaths
        every { _toolEnvironment.defaultPaths } returns defaultPaths
        every { _toolEnvironment.environmentPaths } returns environmentPaths
        every { _toolSearchService.find(MonoConstants.RUNNER_TYPE, capture(paths)) } returns sequenceOf(File("mono"))
        val actualPath = toolProvider.getPath(MonoConstants.RUNNER_TYPE)

        // Then
        Assert.assertEquals(paths[0].toList(), (homePaths + defaultPaths + environmentPaths).toList())
        Assert.assertEquals(actualPath, File("mono").canonicalPath)
    }

    @Test
    fun shouldNotSearchToolInVirtualContext() {
        // Given
        every { _toolProvidersRegistry.registerToolProvider(any()) } returns Unit
        val context = mockk<BuildRunnerContext>() {
            every { virtualContext } returns mockk<VirtualContext>() {
                every { isVirtual } returns true
            }
        }

        val toolProvider = createInstance()
        val path = toolProvider.getPath("mono", mockk<AgentRunningBuild>(), context)

        Assert.assertEquals(path, "mono")
    }

    private fun createInstance(): ToolProvider =
            MonoToolProvider(
                    _toolProvidersRegistry,
                    _toolSearchService,
                    _toolEnvironment)
}