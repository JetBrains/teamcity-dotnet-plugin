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
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommandSetTest {
    private lateinit var _context: DotnetBuildContext
    @MockK private lateinit var _environmentBuilder: EnvironmentBuilder
    @MockK private lateinit var _toolStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _buildCommand: DotnetCommand
    @MockK private lateinit var _cleanCommand: DotnetCommand
    @MockK private lateinit var _dotnetCommand: DotnetCommand
    @MockK private lateinit var _testCommand: TestCommand

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _buildCommand.commandType } returns DotnetCommandType.Build
        every { _cleanCommand.commandType } returns DotnetCommandType.Clean
        every { _testCommand.commandType } returns DotnetCommandType.Test
        _context = DotnetBuildContext(ToolPath(Path("wd")), _dotnetCommand)
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "clean")), listOf("clean", "CleanArg1", "CleanArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "test")), listOf("test", "TestArg1", "TestArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "build")), listOf("my.csprog", "BuildArg1", "BuildArg2"), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "send")), emptyList<String>() as Any?, null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "   ")), emptyList<String>() as Any?, null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "")), emptyList<String>() as Any?, null),
                arrayOf(emptyMap<String, String>(), emptyList<String>() as Any?, null))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>,
            exceptionPattern: Regex?) {
        // Given
        every { _buildCommand.toolResolver } returns ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")),false, _toolStateWorkflowComposer)
        every { _buildCommand.getArguments(any()) } returns sequenceOf(CommandLineArgument("BuildArg1"), CommandLineArgument("BuildArg2"))
        every { _buildCommand.targetArguments } returns sequenceOf(TargetArguments(sequenceOf(CommandLineArgument("my.csprog", CommandLineArgumentType.Target))))
        every { _buildCommand.environmentBuilders } returns sequenceOf(_environmentBuilder)

        every { _cleanCommand.toolResolver } returns ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")),true, _toolStateWorkflowComposer)
        every { _cleanCommand.getArguments(any()) } returns sequenceOf(CommandLineArgument("CleanArg1"), CommandLineArgument("CleanArg2"))
        every { _cleanCommand.targetArguments } returns emptySequence<TargetArguments>()
        every { _cleanCommand.environmentBuilders } returns emptySequence<EnvironmentBuilder>()

        every { _testCommand.toolResolver } returns ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")),true, _toolStateWorkflowComposer)
        every { _testCommand.getArguments(any()) } returns sequenceOf(CommandLineArgument("TestArg1"), CommandLineArgument("TestArg2"))
        every { _testCommand.targetArguments } returns emptySequence<TargetArguments>()
        every { _testCommand.environmentBuilders } returns emptySequence<EnvironmentBuilder>()

        val dotnetCommandSet = DotnetCommandSet(
                ParametersServiceStub(parameters),
                listOf(_buildCommand, _cleanCommand, _testCommand))

        // When
        var actualArguments: List<String> = emptyList()
        try {
            actualArguments = dotnetCommandSet.commands.flatMap { it.getArguments(_context) }.map { it.value }.toList()
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        } catch (ex: RunBuildException) {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true)
        }

        // Then
        if (exceptionPattern == null) {
            Assert.assertEquals(actualArguments, expectedArguments)
        }
    }
}