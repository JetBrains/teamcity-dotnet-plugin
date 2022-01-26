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

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotneRuntimeAgentPropertiesProviderTest {
    @MockK private lateinit var _dotnetRuntimesProvider: DotnetRuntimesProvider
    @MockK private lateinit var _versionEnumerator: VersionEnumerator
    private val _toolPath = Path("dotnet")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<List<Any>>> {
        return arrayOf(
                arrayOf(
                        emptyList<DotnetRuntime>(),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        listOf(
                                DotnetRuntime(File("1.2.3"), Version(1, 2, 3), "NETCore")),
                        listOf(
                                AgentProperty(ToolInstanceType.DotNetRuntime, "${DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME}1.2${DotnetConstants.CONFIG_SUFFIX_PATH}", File("1.2.3").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetRuntime, "${DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME}1.2.3${DotnetConstants.CONFIG_SUFFIX_PATH}", File("1.2.3").absolutePath)))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideAgentProperties(
            runtimes: List<DotnetRuntime>,
            expectedProperties: List<AgentProperty>) {
        // Given
        every { _dotnetRuntimesProvider.getRuntimes() } returns runtimes.asSequence()
        every { _versionEnumerator.enumerate<DotnetRuntime>(any()) } answers {
            sequence {
                for (runtime in arg<Sequence<DotnetRuntime>>(0)) {
                    yield(Pair("${runtime.version.major}.${runtime.version.minor}", runtime))
                    yield(Pair(runtime.version.toString(), runtime))
                }
            }
        }

        val propertiesProvider = createInstance()
        val actualDescription = propertiesProvider.desription
        val actualProperties = propertiesProvider.properties.toList()

        // When
        Assert.assertEquals(actualDescription, ".NET Runtime")
        Assert.assertEquals(actualProperties, expectedProperties)
    }

    private fun createInstance() = DotnetRuntimeAgentPropertiesProvider(
            _dotnetRuntimesProvider,
            _versionEnumerator)
}