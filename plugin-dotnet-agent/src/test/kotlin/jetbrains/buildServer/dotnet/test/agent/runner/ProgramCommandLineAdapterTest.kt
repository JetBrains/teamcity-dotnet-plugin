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

package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.ProgramCommandLineAdapter
import jetbrains.buildServer.agent.runner.ProgramCommandLineAdapter.Companion.ENV_DOCKER_QUIET_MODE
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ProgramCommandLineAdapterTest {
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _buildStepContext: BuildStepContext
    @MockK private lateinit var _virtualContext: VirtualContext

    private val _executable = Path("executable")
    private val _workingDirecory = Path("wd")
    private val _args = listOf(
        CommandLineArgument("Arg1"),
        CommandLineArgument("Arg 2")
    )
    private val _baseVars = mapOf(
            "Var1" to "Val1",
            "Var 3" to "Val 3"
    )
    private val _envVars = listOf(
            CommandLineEnvironmentVariable("Var1", "Val1"),
            CommandLineEnvironmentVariable("Var 2", "Val 2")
    )
    private val _commandLine = CommandLine(
            null,
            TargetType.Tool,
            _executable,
            _workingDirecory,
            _args,
            _envVars)

    private val _systemDiagnosticsCommandLine = CommandLine(
            null,
            TargetType.SystemDiagnostics,
            _executable,
            _workingDirecory,
            _args,
            _envVars)

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _buildStepContext.runnerContext } returns mockk<BuildRunnerContext>() {
            every { buildParameters } returns mockk<BuildParametersMap>() {
                every { environmentVariables } returns _baseVars
            }
        }
        every { _argumentsService.normalize(any()) } answers {
            val arg = arg<String>(0)
            if(arg.contains(' ')) "\"$arg\"" else arg
        }
    }

    @DataProvider(name = "testData")
    fun osTypesData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.UNIX, listOf("Arg1", "Arg 2")),
                arrayOf(OSType.MAC, listOf("Arg1", "Arg 2")),
                arrayOf(OSType.WINDOWS, listOf("Arg1", "\"Arg 2\"")))
    }

    @Test(dataProvider = "testData")
    fun shouldAdaptToProgramCommandLineAdapter(os: OSType, expectedArgs: List<String>) {
        // Given

        // When
        every { _environment.os } returns os
        every { _virtualContext.isVirtual } returns false
        val programCommandLine = createInstance(_commandLine)

        // Then
        Assert.assertEquals(programCommandLine.executablePath, _executable.path)
        Assert.assertEquals(programCommandLine.workingDirectory, _workingDirecory.path)
        Assert.assertEquals(programCommandLine.arguments, expectedArgs)
        Assert.assertEquals(programCommandLine.environment.entries, mapOf(
                "Var1" to "Val1",
                "Var 3" to "Val 3",
                "Var 2" to "Val 2"
        ).entries)
    }

    @DataProvider(name = "osData")
    fun osData(): Array<Array<OSType>> {
        return arrayOf(
                arrayOf(OSType.UNIX),
                arrayOf(OSType.MAC),
                arrayOf(OSType.WINDOWS))
    }

    @Test(dataProvider = "osData")
    fun shouldAddEnvVar_TEAMCITY_DOCKER_QUIET_MODE_WhenVirtaulContextAndSystemDiagnosticsCommand(os: OSType) {
        // Given

        // When
        every { _environment.os } returns os
        every { _virtualContext.isVirtual } returns true
        val programCommandLine = createInstance(_systemDiagnosticsCommandLine)

        // Then
        Assert.assertEquals(programCommandLine.environment.entries, mapOf(
                "Var1" to "Val1",
                "Var 3" to "Val 3",
                "Var 2" to "Val 2",
                ENV_DOCKER_QUIET_MODE to "true"
        ).entries)
    }

    private fun createInstance(commandLine: CommandLine): ProgramCommandLine =
            ProgramCommandLineAdapter(_argumentsService, _environment, _buildStepContext, _virtualContext)
                    .create(commandLine)
}