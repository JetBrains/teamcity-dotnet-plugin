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

package jetbrains.buildServer.dotnet.test.agent

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.rx.subjectOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.testng.Assert
import org.testng.annotations.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PropertiesExtensionTest {
    private val mainThreadSurrogate = newSingleThreadContext("Main thread")
    @MockK private lateinit var _eventSources: EventSources
    @MockK private lateinit var _buildAgent: BuildAgent
    @MockK private lateinit var _buildAgentConfiguration: BuildAgentConfiguration
    @MockK private lateinit var _agentPropertiesProvider1: AgentPropertiesProvider
    @MockK private lateinit var _agentPropertiesProvider2: AgentPropertiesProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _buildAgent.configuration } returns _buildAgentConfiguration
    }

    @BeforeClass
    fun setUpClass() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterClass
    fun tearDownClass() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun shouldAddOrUpdateConfigParams() {
        // Given
        val beforeAgentConfigurationLoadedSource = subjectOf<EventSources.BeforeAgentConfigurationLoaded>()
        every { _eventSources.beforeAgentConfigurationLoadedSource } returns beforeAgentConfigurationLoadedSource

        var config = mutableMapOf<String, String>();
        every { _buildAgentConfiguration.addConfigurationParameter(any(), any()) } answers {
            config[arg<String>(0)] = arg<String>(1)
            Unit
        }
        every { _buildAgentConfiguration.configurationParameters } returns config

        every { _agentPropertiesProvider1.desription } returns "1"
        every { _agentPropertiesProvider1.properties } returns sequenceOf(AgentProperty(ToolInstanceType.DotNetCLI, "prop1", "val1"), AgentProperty(ToolInstanceType.DotNetCLI, "prop", "val"))

        every { _agentPropertiesProvider2.desription } returns "2"
        every { _agentPropertiesProvider2.properties } returns sequenceOf(AgentProperty(ToolInstanceType.DotNetCLI, "prop", "val"), AgentProperty(ToolInstanceType.DotNetCLI, "prop2", "val2"))

        createInstance()

        // When
        beforeAgentConfigurationLoadedSource.onNext(EventSources.BeforeAgentConfigurationLoaded(_buildAgent))

        // Then
        Assert.assertEquals(config.size, 3)
        Assert.assertEquals(config["prop1"], "val1")
        Assert.assertEquals(config["prop2"], "val2")
        Assert.assertEquals(config["prop"], "val")
    }

    private fun createInstance(): PropertiesExtension {
        val propertiesExtension = PropertiesExtension(Dispatchers.Main, listOf(_agentPropertiesProvider1, _agentPropertiesProvider2))
        propertiesExtension.subscribe(_eventSources)
        return propertiesExtension
    }
}