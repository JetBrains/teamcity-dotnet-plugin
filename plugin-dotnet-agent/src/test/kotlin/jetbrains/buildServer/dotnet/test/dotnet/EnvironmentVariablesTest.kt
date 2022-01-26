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
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.EnvironmentVariablesImpl.Companion.UseSharedCompilationEnvVarName
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class EnvironmentVariablesTest {
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _nugetEnvironmentVariables: EnvironmentVariables
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _loggerResolver: LoggerResolver

    private val _tmpPath = File("Tmp")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _nugetEnvironmentVariables.getVariables(any()) } returns emptySequence()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
        every { _parametersService.tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName) } returns null
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MESSAGES_GUARD) } returns null
        every { _loggerResolver.resolve(ToolType.MSBuild) } returns File("msbuild_logger");
        every { _loggerResolver.resolve(ToolType.VSTest) } returns File("vstest_logger");
        every { _pathsService.getPath(PathType.AgentTemp) } returns _tmpPath
    }

    @Test
    fun shouldProvideDefaultVars() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _nugetEnvironmentVariables.getVariables(any()) } returns sequenceOf(CommandLineEnvironmentVariable("NUGET_VAR", nugetPath))
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + commonVars + sequenceOf(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, "false"), CommandLineEnvironmentVariable("NUGET_VAR", nugetPath))).toList())
    }

    @Test
    fun shouldNotPublishedMessageGuardPathWhenItIsNotAllowed() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _nugetEnvironmentVariables.getVariables(any()) } returns sequenceOf(CommandLineEnvironmentVariable("NUGET_VAR", nugetPath))
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MESSAGES_GUARD) } returns "false"

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(
                actualVariables,
                (
                        EnvironmentVariablesImpl.defaultVariables
                                + sequenceOf(
                                    CommandLineEnvironmentVariable(EnvironmentVariablesImpl.MSBuildLoggerEnvVar, File("msbuild_logger").canonicalPath),
                                    CommandLineEnvironmentVariable(EnvironmentVariablesImpl.VSTestLoggerEnvVar, File("vstest_logger").canonicalPath))
                                + sequenceOf(
                                    CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, "false"),
                                    CommandLineEnvironmentVariable("NUGET_VAR", nugetPath))
                ).toList()
        )
    }

    @Test
    fun shouldNotUseSharedCompilationByDefault() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + commonVars + sequenceOf(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, "false"))).toList())
    }

    @Test
    fun shouldUseSharedCompilationWhenThisParameterWasOverridedInEnvVars() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        every { _parametersService.tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName) } returns "true"

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + commonVars + sequenceOf(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, "true"))).toList())
    }

    @DataProvider(name = "osTypesData")
    fun osTypesData(): Array<Array<OSType>> {
        return arrayOf(
                arrayOf(OSType.UNIX),
                arrayOf(OSType.MAC))
    }

    @Test(dataProvider = "osTypesData")
    fun shouldProvideDefaultVarsWhenVirtualContextFromWindows(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + commonVars + sequenceOf(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, "false")) + EnvironmentVariablesImpl.getTempDirVariables()).toList())
    }

    @Test
    fun shouldProvideDefaultVarsWhenVirtualContextForWindowsContainer() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        
        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + commonVars + sequenceOf(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, "false"))).toList())
    }

    @Test(dataProvider = "osTypesData")
    fun shouldNotOverrideTeamCityTempWhenNotVirtualAndNotWindowsAndLenghtLessOrEq60(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val tempPath = "a".repeat(60)

        // When
        every { _environment.os } returns os
        every { _environment.tryGetVariable("HOME") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _pathsService.getPath(PathType.BuildTemp) } returns File(tempPath)

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        for (envVar in EnvironmentVariablesImpl.getTempDirVariables(tempPath)) {
            Assert.assertTrue(!actualVariables.contains(envVar))
        }
    }

    @Test(dataProvider = "osTypesData")
    fun shouldOverrideTeamCityTempByTmpWhenNotVirtualAndNotWindowsAndLenghtLessMore60AndTmpExists(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val tempPath = "a".repeat(61)

        // When
        every { _environment.os } returns os
        every { _environment.tryGetVariable("HOME") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _pathsService.getPath(PathType.BuildTemp) } returns File(tempPath)
        every { _fileSystemService.isExists(EnvironmentVariablesImpl.defaultTemp) } returns true
        every { _fileSystemService.isDirectory(EnvironmentVariablesImpl.defaultTemp) } returns true

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        for (envVar in EnvironmentVariablesImpl.getTempDirVariables(EnvironmentVariablesImpl.defaultTemp.path)) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    @Test(dataProvider = "osTypesData")
    fun shouldOverrideTeamCityTempByCustomTeamCityTempWhenNotVirtualAndNotWindowsAndLenghtLessMore60AndTmpExists(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val tempPath = "a".repeat(61)

        // When
        every { _environment.os } returns os
        every { _environment.tryGetVariable("HOME") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _pathsService.getPath(PathType.BuildTemp) } returns File(tempPath)
        every { _fileSystemService.isExists(EnvironmentVariablesImpl.defaultTemp) } returns false
        every { _fileSystemService.isExists(EnvironmentVariablesImpl.customTeamCityTemp) } returns false
        every { _fileSystemService.createDirectory(EnvironmentVariablesImpl.customTeamCityTemp) } returns true

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        for (envVar in EnvironmentVariablesImpl.getTempDirVariables(EnvironmentVariablesImpl.customTeamCityTemp.path)) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    @Test
    fun shouldOverrideDefaultNugetPackagesPathWhenSpecifiedAsEnvVar() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")

        // When
        every { _nugetEnvironmentVariables.getVariables(any()) } returns sequenceOf(CommandLineEnvironmentVariable("NUGET_VAR", "custom_nuget_packages_path"))
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + commonVars + sequenceOf(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, "false"), CommandLineEnvironmentVariable("NUGET_VAR", "custom_nuget_packages_path"))).toList())
    }

    private fun createInstance() = EnvironmentVariablesImpl(
            _environment,
            _parametersService,
            _pathsService,
            _fileSystemService,
            _nugetEnvironmentVariables,
            _virtualContext,
            _loggerResolver)

    private val commonVars = sequenceOf(
        CommandLineEnvironmentVariable(EnvironmentVariablesImpl.MSBuildLoggerEnvVar, File("msbuild_logger").canonicalPath),
        CommandLineEnvironmentVariable(EnvironmentVariablesImpl.VSTestLoggerEnvVar, File("vstest_logger").canonicalPath),
        CommandLineEnvironmentVariable(EnvironmentVariablesImpl.ServiceMessagesPathEnvVar, _tmpPath.canonicalPath)
    )
}