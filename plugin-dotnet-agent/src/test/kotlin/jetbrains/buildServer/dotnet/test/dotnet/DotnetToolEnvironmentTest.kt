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
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetToolEnvironment
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetToolEnvironmentTest {
    @MockK private lateinit var _buildStepContext: BuildStepContext
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "homePaths")
    fun homePaths(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(null, emptyList<Path>()),
                arrayOf("Program", listOf(Path("Program"))))
    }

    @Test(dataProvider = "homePaths")
    fun shouldProvideHomePathsWhenOutOfBuildStep(homeVar: String?, expectedPaths: List<File>) {
        // Given
        every { _buildStepContext.isAvailable } returns false
        every { _environment.tryGetVariable(DotnetConstants.TOOL_HOME) } returns homeVar

        // When
        val actualPaths = createInstance().homePaths.toList()

        // Then
        Assert.assertEquals(actualPaths, expectedPaths)
    }

    @Test(dataProvider = "homePaths")
    fun shouldProvideHomePathsWhenInfBuildStep(homeVar: String?, expectedPaths: List<File>) {
        // Given
        every { _buildStepContext.isAvailable } returns true
        every { _parametersService.tryGetParameter(ParameterType.Environment, DotnetConstants.TOOL_HOME) } returns homeVar

        // When
        val actualPaths = createInstance().homePaths.toList()

        // Then
        Assert.assertEquals(actualPaths, expectedPaths)
    }

    @DataProvider(name = "defaultPaths")
    fun defaultPaths(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(OSType.WINDOWS, null, listOf(Path("C:\\Program Files\\${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}"))),
                arrayOf(OSType.WINDOWS, "D:\\Program", listOf(Path("D:\\Program\\${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}"))),
                arrayOf(OSType.UNIX, null, listOf(Path("/usr/share/${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}"))),
                arrayOf(OSType.MAC, null, listOf(Path("/usr/local/share/${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}"))))
    }

    @Test(dataProvider = "defaultPaths")
    fun shouldProvideDefaultPaths(os: OSType, proggramFilesEnvVar: String?, expectedPaths: List<File>) {
        // Given
        every { _environment.os } returns os
        every { _environment.tryGetVariable(DotnetConstants.PROGRAM_FILES_ENV_VAR) } returns proggramFilesEnvVar

        // When
        val actualPaths = createInstance().defaultPaths.toList()

        // Then
        Assert.assertEquals(actualPaths, expectedPaths)
    }

    private fun createInstance() =
            DotnetToolEnvironment(_buildStepContext, _environment, _parametersService)
}