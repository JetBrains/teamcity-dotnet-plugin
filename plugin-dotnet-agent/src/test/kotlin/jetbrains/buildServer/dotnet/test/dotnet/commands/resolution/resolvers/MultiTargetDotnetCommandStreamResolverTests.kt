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

package jetbrains.buildServer.dotnet.test.dotnet.commands.resolution.resolvers

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.resolvers.MultiTargetDotnetCommandResolver
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class MultiTargetDotnetCommandStreamResolverTests {
    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should be on Targeting stage`() {
        // arrange
        val resolver = create()

        // act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsResolvingStage.Targeting)
    }

    @Test
    fun `should not be applied if target arguments less then 2`() {
        // arrange
        val commandMock = mockk<DotnetCommand>()
        every { commandMock.targetArguments } answers { sequenceOf(mockk()) }
        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(commandMock)).toList()

        // assert
        Assert.assertEquals(result.size, 1)
        Assert.assertSame(result[0], commandMock)
    }

    @Test
    fun `should resolve one command to multiple if it has target arguments more then 1`() {
        // arrange
        val (commandMock1, commandMock2) = Pair(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        val (targetArgumentsMock1, targetArgumentsMock2) = Pair(mockk<TargetArguments>(), mockk<TargetArguments>())
        val (commandLineArgsMock1, commandLineArgsMock2) = Pair(mockk<Sequence<CommandLineArgument>>(), mockk<Sequence<CommandLineArgument>>())
        every { commandMock1.targetArguments } answers { sequenceOf(targetArgumentsMock1) }
        every { commandMock1.getArguments(any()) } answers { commandLineArgsMock1 }
        every { commandMock2.targetArguments } answers { sequenceOf(targetArgumentsMock1, targetArgumentsMock2) }
        every { commandMock2.getArguments(any()) } answers { commandLineArgsMock2 }
        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(commandMock1, commandMock2)).toList()

        // assert
        Assert.assertEquals(result.size, 3)
        result.forEach {
            Assert.assertTrue(it is MultiTargetDotnetCommandResolver.SpecificTargetDotnetCommand)
            Assert.assertEquals(it.targetArguments.count(), 1)
        }
        Assert.assertEquals(result[0].targetArguments.first(), targetArgumentsMock1)
        Assert.assertEquals(result[0].getArguments(mockk()), commandLineArgsMock1)
        Assert.assertEquals(result[1].targetArguments.first(), targetArgumentsMock1)
        Assert.assertEquals(result[1].getArguments(mockk()), commandLineArgsMock2)
        Assert.assertEquals(result[2].targetArguments.first(), targetArgumentsMock2)
        Assert.assertEquals(result[2].getArguments(mockk()), commandLineArgsMock2)
    }

    @Test
    fun `should resolve even if target arguments of original command are empty`() {
        // arrange
        val (commandMock1, commandMock2) = Pair(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        val (targetArgumentsMock1, targetArgumentsMock2) = Pair(mockk<TargetArguments>(), mockk<TargetArguments>())
        val (commandLineArgsMock1, commandLineArgsMock2) = Pair(mockk<Sequence<CommandLineArgument>>(), mockk<Sequence<CommandLineArgument>>())
        every { commandMock1.targetArguments } answers { emptySequence() }
        every { commandMock1.getArguments(any()) } answers { commandLineArgsMock1 }
        every { commandMock2.targetArguments } answers { sequenceOf(targetArgumentsMock1, targetArgumentsMock2) }
        every { commandMock2.getArguments(any()) } answers { commandLineArgsMock2 }
        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(commandMock1, commandMock2)).toList()

        // assert
        Assert.assertEquals(result.size, 3)
        result.forEach {
            Assert.assertTrue(it is MultiTargetDotnetCommandResolver.SpecificTargetDotnetCommand)
            Assert.assertEquals(it.targetArguments.count(), 1)
        }
        Assert.assertEquals(result[0].targetArguments.first().arguments.count(), 0)
        Assert.assertEquals(result[0].getArguments(mockk()), commandLineArgsMock1)
        Assert.assertEquals(result[1].targetArguments.first(), targetArgumentsMock1)
        Assert.assertEquals(result[1].getArguments(mockk()), commandLineArgsMock2)
        Assert.assertEquals(result[2].targetArguments.first(), targetArgumentsMock2)
        Assert.assertEquals(result[2].getArguments(mockk()), commandLineArgsMock2)
    }

    private fun create() = MultiTargetDotnetCommandResolver()
}