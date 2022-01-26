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
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.NugetEnvironmentVariables.Companion.FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR
import jetbrains.buildServer.dotnet.NugetEnvironmentVariables.Companion.NUGET_HTTP_CACHE_PATH_ENV_VAR
import jetbrains.buildServer.dotnet.NugetEnvironmentVariables.Companion.NUGET_PACKAGES_ENV_VAR
import jetbrains.buildServer.dotnet.NugetEnvironmentVariables.Companion.NUGET_PLUGIN_PATH_ENV_VAR
import jetbrains.buildServer.dotnet.NugetEnvironmentVariables.Companion.NUGET_RESTORE_MSBUILD_VERBOSITY_ENV_VAR
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class NuGetEnvironmentVariablesTest {
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _nugetCredentialProviderSelector: NugetCredentialProviderSelector
    @MockK private lateinit var _nugetEnvironment: NugetEnvironment

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
        every { _nugetCredentialProviderSelector.trySelect(any()) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_OVERRIDE_NUGET_VARS) } returns null
        every { _nugetEnvironment.allowInternalCaches } returns true

        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns null
        every { _environment.tryGetVariable(NUGET_RESTORE_MSBUILD_VERBOSITY_ENV_VAR) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Environment, NUGET_RESTORE_MSBUILD_VERBOSITY_ENV_VAR) } returns null

        every { _environment.tryGetVariable(FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Environment, FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR) } returns null

        every { _environment.tryGetVariable(NUGET_HTTP_CACHE_PATH_ENV_VAR) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Environment, NUGET_HTTP_CACHE_PATH_ENV_VAR) } returns null

        every { _environment.tryGetVariable(NUGET_PACKAGES_ENV_VAR) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Environment, NUGET_PACKAGES_ENV_VAR) } returns null

        every { _environment.tryGetVariable(NUGET_PLUGIN_PATH_ENV_VAR) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Environment, NUGET_PLUGIN_PATH_ENV_VAR) } returns null
    }

    @Test
    fun shouldProvideDefaultVars() {
        // Given
        val systemPath = File("System")

        // When
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _nugetCredentialProviderSelector.trySelect(any()) } returns "CredentilProvider.dll"
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Detailed.id

        val actualPaths = createInstance().getVariables(Version.Empty).toList()

        // Then
        Assert.assertEquals(
                actualPaths,
                listOf(
                        CommandLineEnvironmentVariable(FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR, ""),
                        CommandLineEnvironmentVariable(NUGET_HTTP_CACHE_PATH_ENV_VAR, "v_" + File(File(systemPath, "dotnet"), ".http").canonicalPath),
                        CommandLineEnvironmentVariable(NUGET_PACKAGES_ENV_VAR, "v_" + File(File(systemPath, "dotnet"), ".nuget").canonicalPath),
                        CommandLineEnvironmentVariable(NUGET_PLUGIN_PATH_ENV_VAR, "v_CredentilProvider.dll"),
                        CommandLineEnvironmentVariable(NUGET_RESTORE_MSBUILD_VERBOSITY_ENV_VAR, Verbosity.Detailed.id)))
    }

    @Test
    fun shouldSkipCachePathVarsWhenItIsNotAllowed() {
        // Given
        val systemPath = File("System")

        // When
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _nugetCredentialProviderSelector.trySelect(any()) } returns "CredentilProvider.dll"
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Detailed.id
        every { _nugetEnvironment.allowInternalCaches } returns false

        val actualPaths = createInstance().getVariables(Version.Empty).toList()

        // Then
        Assert.assertEquals(
                actualPaths,
                listOf(
                        CommandLineEnvironmentVariable(FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR, ""),
                        CommandLineEnvironmentVariable(NUGET_PLUGIN_PATH_ENV_VAR, "v_CredentilProvider.dll"),
                        CommandLineEnvironmentVariable(NUGET_RESTORE_MSBUILD_VERBOSITY_ENV_VAR, Verbosity.Detailed.id)))
    }

    @Test
    fun shouldShouldNotProvideSomeVars() {
        // Given
        val systemPath = File("System")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_OVERRIDE_NUGET_VARS) } returns " ${FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR.toLowerCase()};$NUGET_HTTP_CACHE_PATH_ENV_VAR"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _nugetCredentialProviderSelector.trySelect(any()) } returns "CredentilProvider.dll"
        val actualPaths = createInstance().getVariables(Version.Empty).toList()

        // Then
        Assert.assertEquals(
                actualPaths,
                listOf(
                        CommandLineEnvironmentVariable(FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR, ""),
                        CommandLineEnvironmentVariable(NUGET_HTTP_CACHE_PATH_ENV_VAR, "v_" + File(File(systemPath, "dotnet"), ".http").canonicalPath)))
    }

    @Test
    fun shouldProvideCachePathsWhenItWasOverridedByEnvVarFromEnvironment() {
        // Given

        // When
        every { _environment.tryGetVariable(NugetEnvironmentVariables.FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR) } returns "custom_var"
        every { _environment.tryGetVariable(NugetEnvironmentVariables.NUGET_PACKAGES_ENV_VAR) } returns "custom_path"
        every { _environment.tryGetVariable(NugetEnvironmentVariables.NUGET_HTTP_CACHE_PATH_ENV_VAR) } returns "custom_path_http"

        val actualPaths = createInstance().getVariables(Version.Empty).toList()

        // Then
        Assert.assertEquals(actualPaths, emptyList<Path>())
    }

    @Test
    fun shouldProvideCachePathsWhenItWasOverridedByEnvVarFromTeamCity() {
        // Given

        // When
        every { _parametersService.tryGetParameter(ParameterType.Environment, NugetEnvironmentVariables.FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR) } returns "custom_var"
        every { _parametersService.tryGetParameter(ParameterType.Environment, NugetEnvironmentVariables.NUGET_PACKAGES_ENV_VAR) } returns "custom_path"
        every { _parametersService.tryGetParameter(ParameterType.Environment, NugetEnvironmentVariables.NUGET_HTTP_CACHE_PATH_ENV_VAR) } returns "custom_path_http"

        val actualPaths = createInstance().getVariables(Version.Empty).toList()

        // Then
        Assert.assertEquals(actualPaths, emptyList<Path>())
    }

    @Test
    fun shouldNotProvideDefaultCachePathsWhenTheOverridingIsNotAlloing() {
        // Given
        val systemPath = File("System")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_OVERRIDE_NUGET_VARS) } returns "FalSe"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        val actualPaths = createInstance().getVariables(Version.Empty).toList()

        // Then
        Assert.assertEquals(actualPaths, emptyList<Path>())
    }

    private fun createInstance() =
            NugetEnvironmentVariables(
                    _environment,
                    _parametersService,
                    _pathsService,
                    _virtualContext,
                    _nugetCredentialProviderSelector,
                    _nugetEnvironment)
}