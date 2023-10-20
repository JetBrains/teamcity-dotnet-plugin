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
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.common.MSBuildEnvironmentVariables
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.inspect.InspectCodeEnvironmentProvider.Companion.PLUGIN_LIST_ENV_VAR
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class InspectCodeEnvironmentProviderTest {
    @MockK
    private lateinit var _pluginsSpecificationProvider: PluginsSpecificationProvider

    @MockK
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun getJetAdditionalDeployedPackagesEnvVarTestCases() = arrayOf(
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

    @Test(dataProvider = "getJetAdditionalDeployedPackagesEnvVarTestCases")
    fun `should provide JET_ADDITIONAL_DEPLOYED_PACKAGES environment variable only when has plugins and version corresponds`(
        pluginsSpecification: String?,
        toolVersion: Version,
        expectedVars: Collection<CommandLineEnvironmentVariable>
    ) {
        // arrange
        val environmentProvider = createInstance()
        every { _pluginsSpecificationProvider.getPluginsSpecification() } answers { pluginsSpecification }
        every { _parametersService.tryGetParameter(any(), any()) } answers { null }

        // act
        val actualVars = environmentProvider.getEnvironmentVariables(toolVersion, InspectionToolPlatform.CrossPlatform).toList()

        // assert
        Assert.assertTrue(actualVars.containsAll(expectedVars))
    }

    @DataProvider
    fun getUseSharedCompilationEnvVarTestCases() = arrayOf(
        arrayOf(
            InspectionToolPlatform.WindowsX64,
            null,
            listOf(CommandLineEnvironmentVariable(MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR, "false")),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX86,
            null,
            listOf(CommandLineEnvironmentVariable(MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR, "false")),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX86,
            " ",
            listOf(CommandLineEnvironmentVariable(MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR, "false")),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX86,
            "",
            listOf(CommandLineEnvironmentVariable(MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR, "false")),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX64,
            "true",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX86,
            "true",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX64,
            "false",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX86,
            "false",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX64,
            "abc",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.WindowsX86,
            "abc",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.CrossPlatform,
            null,
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.CrossPlatform,
            "true",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.CrossPlatform,
            "false",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
        arrayOf(
            InspectionToolPlatform.CrossPlatform,
            "abc",
            emptyList<CommandLineEnvironmentVariable>(),
        ),
    )

    @Test(dataProvider = "getUseSharedCompilationEnvVarTestCases")
    fun `should provide UseSharedCompilation environment variable as false by default when tool platform is Windows and allow users to override`(
        toolPlatform: InspectionToolPlatform,
        useSharedCompilationParam: String?,
        expectedVars: Collection<CommandLineEnvironmentVariable>
    ) {
        // arrange
        val environmentProvider = createInstance()
        every { _pluginsSpecificationProvider.getPluginsSpecification() } answers { null }
        every { _parametersService.tryGetParameter(any(), any()) } answers { useSharedCompilationParam }

        // act
        val actualVars = environmentProvider.getEnvironmentVariables(Version.FirstInspectCodeWithExtensionsOptionVersion, toolPlatform).toList()

        // assert
        Assert.assertTrue(actualVars.containsAll(expectedVars))
    }

    @Test
    fun `should provide all env variables together`() {
        // arrange
        val environmentProvider = createInstance()
        every { _pluginsSpecificationProvider.getPluginsSpecification() } answers { "Plugins" }
        every { _parametersService.tryGetParameter(any(), any()) } answers { null }
        val expectedVars = listOf(
            CommandLineEnvironmentVariable(PLUGIN_LIST_ENV_VAR, "Plugins"),
            CommandLineEnvironmentVariable(MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR, "false")
        )

        // act
        val actualVars = environmentProvider.getEnvironmentVariables(Version.Empty, InspectionToolPlatform.WindowsX64).toList()

        // assert
        Assert.assertTrue(actualVars.containsAll(expectedVars))
    }

    private fun createInstance() = InspectCodeEnvironmentProvider(_pluginsSpecificationProvider, _parametersService)
}