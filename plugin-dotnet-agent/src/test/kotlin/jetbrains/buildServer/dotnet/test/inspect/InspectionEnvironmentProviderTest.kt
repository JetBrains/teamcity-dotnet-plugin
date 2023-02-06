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

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.E
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS
import jetbrains.buildServer.inspect.InspectionEnvironmentProvider.Companion.PluginListEnvVar
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.OutputStream

class InspectionEnvironmentProviderTest {
    @MockK private lateinit var _pluginsListProvider: PackagesProvider
    @MockK private lateinit var _xmlWriter: XmlWriter

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "envVarsCases")
    fun getEnvVarsCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        ParametersServiceStub(mapOf(RUNNER_SETTING_CLT_PLUGINS to "Plugins")),
                        listOf<CommandLineEnvironmentVariable>(CommandLineEnvironmentVariable(PluginListEnvVar, "Plugins"))
                ),
                arrayOf(
                        ParametersServiceStub(emptyMap()),
                        emptyList<CommandLineEnvironmentVariable>()
                )
        )
    }

    @Test(dataProvider = "envVarsCases")
    fun shouldGetEnvironmentVariables(
            parametersService: ParametersService,
            expectedVars: Collection<CommandLineEnvironmentVariable>) {
        // Given
        val environmentProvider = createInstance(parametersService)
        every { _pluginsListProvider.getPackages(any()) } answers {
            E("Packages", arg<String>(0))
        }

        every { _xmlWriter.write(any(), any()) } answers {
            val stream = arg<OutputStream>(1)
            val element = arg<E>(0)
            stream.writer().use {
                it.write(element.value ?: "")
            }
        }

        // When
        val actualVars = environmentProvider.getEnvironmentVariables().toList()

        // Then
        Assert.assertEquals(actualVars, expectedVars)
    }

    private fun createInstance(parametersService: ParametersService) =
            InspectionEnvironmentProvider(
                    parametersService,
                    _pluginsListProvider,
                    _xmlWriter
            )
}