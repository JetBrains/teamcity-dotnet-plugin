/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommandSetTest {
    private lateinit var _ctx: Mockery
    private lateinit var _buildCommand: DotnetCommand
    private lateinit var _cleanCommand: DotnetCommand
    private lateinit var _environmentBuilder: EnvironmentBuilder

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _buildCommand = _ctx.mock<DotnetCommand>(DotnetCommand::class.java, "Build")
        _cleanCommand = _ctx.mock<DotnetCommand>(DotnetCommand::class.java, "Clean")
        _environmentBuilder = _ctx.mock(EnvironmentBuilder::class.java)
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_COMMAND, "clean")), listOf("clean", "CleanArg1", "CleanArg2"), null),
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
        val context = DotnetBuildContext(ToolPath(Path("wd")), _ctx.mock(DotnetCommand::class.java))
        _ctx.checking(object : Expectations() {
            init {
                allowing<DotnetCommand>(_buildCommand).commandType
                will(returnValue(DotnetCommandType.Build))

                allowing<DotnetCommand>(_buildCommand).toolResolver
                will(returnValue(DotnetToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")),false)))

                allowing<DotnetCommand>(_buildCommand).getArguments(context)
                will(returnValue(sequenceOf(CommandLineArgument("BuildArg1"), CommandLineArgument("BuildArg2"))))

                allowing<DotnetCommand>(_buildCommand).targetArguments
                will(returnValue(sequenceOf(TargetArguments(sequenceOf(CommandLineArgument("my.csprog"))))))

                allowing<DotnetCommand>(_buildCommand).environmentBuilders
                will(returnValue(sequenceOf(_environmentBuilder)))

                allowing<DotnetCommand>(_cleanCommand).commandType
                will(returnValue(DotnetCommandType.Clean))

                allowing<DotnetCommand>(_cleanCommand).toolResolver
                will(returnValue(DotnetToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")),true)))

                allowing<DotnetCommand>(_cleanCommand).getArguments(context)
                will(returnValue(sequenceOf(CommandLineArgument("CleanArg1"), CommandLineArgument("CleanArg2"))))

                allowing<DotnetCommand>(_cleanCommand).targetArguments
                will(returnValue(emptySequence<TargetArguments>()))

                allowing<DotnetCommand>(_cleanCommand).environmentBuilders
                will(returnValue(emptySequence<EnvironmentBuilder>()))
            }
        })

        val dotnetCommandSet = DotnetCommandSet(
                ParametersServiceStub(parameters),
                listOf(_buildCommand, _cleanCommand))

        // When
        var actualArguments: List<String> = emptyList()
        try {
            actualArguments = dotnetCommandSet.commands.flatMap { it.getArguments(context) }.map { it.value }.toList()
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        } catch (ex: RunBuildException) {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true)
        }

        // Then
        if (exceptionPattern == null) {
            _ctx.assertIsSatisfied()
            Assert.assertEquals(actualArguments, expectedArguments)
        }
    }
}