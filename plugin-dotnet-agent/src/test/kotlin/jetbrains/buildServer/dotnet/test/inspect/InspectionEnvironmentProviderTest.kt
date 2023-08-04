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
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.inspect.InspectionEnvironmentProvider.Companion.PLUGIN_LIST_ENV_VAR
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class InspectionEnvironmentProviderTest {
    @MockK
    private lateinit var _pluginsSpecificationProvider: PluginsSpecificationProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun getEnvVarsCases() = arrayOf(
        arrayOf(
            "Plugins",
            Version(2018, 1, 1),
            listOf(CommandLineEnvironmentVariable(PLUGIN_LIST_ENV_VAR, "Plugins")),
        ),
        arrayOf(
            "Plugins",
            Version(2023, 1, 1),
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            null,
            Version(2018, 1, 1),
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            null,
            Version(2023, 1, 1),
            emptyList<CommandLineEnvironmentVariable>(),
        )
    )


    @Test(dataProvider = "getEnvVarsCases")
    fun `should provide environment variable when has plugins and version corresponds`(
        pluginsSpecification: String?,
        toolVersion: Version,
        expectedVars: Collection<CommandLineEnvironmentVariable>
    ) {
        // arrange
        val environmentProvider = InspectionEnvironmentProvider(_pluginsSpecificationProvider)
        every { _pluginsSpecificationProvider.getPluginsSpecification() } answers { pluginsSpecification }

        // act
        val actualVars = environmentProvider.getEnvironmentVariables(toolVersion).toList()

        // assert
        Assert.assertEquals(actualVars, expectedVars)
    }
}