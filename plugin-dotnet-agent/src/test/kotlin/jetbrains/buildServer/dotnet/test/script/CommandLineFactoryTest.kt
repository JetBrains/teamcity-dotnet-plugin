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

package jetbrains.buildServer.dotnet.test.script

import io.mockk.InternalPlatformDsl.toArray
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.EnvironmentVariables
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.script.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class CommandLineFactoryTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _toolResolver: ToolResolver
    @MockK private lateinit var _nugetEnvironmentVariables: EnvironmentVariables
    @MockK private lateinit var _rspContentFactory: RspContentFactory
    @MockK private lateinit var _virtualContext: VirtualContext

    private val _envVars = listOf(CommandLineEnvironmentVariable("env1", "val1"))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
        every { _nugetEnvironmentVariables.getVariables(Version(6, 0, 0)) } returns _envVars.asSequence()
        every { _rspContentFactory.create() } returns sequenceOf("Line 1", "Line 2")
    }

    @Test
    fun shouldCreateCommandLine() {
        // Given
        val fileSystemService = VirtualFileSystemService()
        val factory = createInstance(fileSystemService)

        val workingDirectory = File("wd")
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns workingDirectory
        val tmpDirectory = File("tmp")
        every { _pathsService.getPath(PathType.AgentTemp) } returns tmpDirectory

        val tool = CsiTool(File("tool"), Version(6, 0, 0))
        every { _toolResolver.resolve() } returns tool

        // When
        var commandLine = factory.create()

        // Then
        val rspFile = File(tmpDirectory, "options99.rsp")
        Assert.assertEquals(commandLine.baseCommandLine, null)
        Assert.assertEquals(commandLine.target, TargetType.Tool)
        Assert.assertEquals(commandLine.executableFile, Path(""))
        Assert.assertEquals(commandLine.workingDirectory, Path(workingDirectory.path))
        Assert.assertEquals(
                commandLine.arguments,
                listOf(
                        CommandLineArgument("v_tool"),
                        CommandLineArgument("@v_" + rspFile)
                ))
        Assert.assertEquals(commandLine.environmentVariables, _envVars)
        fileSystemService.read(rspFile) {
            BufferedReader(InputStreamReader(it)).use {
                Assert.assertEquals(it.readLines(), listOf("Line 1", "Line 2"))
            }
        }
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            CommandLineFactoryImpl(_pathsService, _toolResolver, _nugetEnvironmentVariables, fileSystemService, _rspContentFactory, _virtualContext)
}