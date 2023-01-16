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

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.resolvers.ParameterBasedDotnetCommandsStreamResolver
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ParameterBasedDotnetCommandsStreamResolverTests {
    @MockK
    private lateinit var _dotnetCommandMock1: DotnetCommand

    @MockK
    private lateinit var _dotnetCommandMock2: DotnetCommand

    @MockK
    private lateinit var _dotnetCommandMock3: DotnetCommand

    @MockK
    private lateinit var _parametersServiceMock: ParametersService

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        every { _dotnetCommandMock1.commandType } answers { DotnetCommandType.Build }
        every { _dotnetCommandMock2.commandType } answers { DotnetCommandType.Test }
        every { _dotnetCommandMock3.commandType } answers { DotnetCommandType.Restore }
    }

    @Test
    fun `should be on CommandRetrieve stage`() {
        // arrange
        val resolver = create()

        // act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsStreamResolvingStage.CommandRetrieve)
    }

    @Test
    fun `should resolve command by valid command id from parameter`() {
        // arrange
        val buildCommandId = DotnetCommandType.Build.id
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { buildCommandId }
        val resolver = create()

        // act
        val result = resolver.resolve().toList()

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.size, 1)
        result.forEach { Assert.assertEquals(it, _dotnetCommandMock1) }
        verify (exactly = 1) { _parametersServiceMock.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND) }
    }

    @Test
    fun `should not resolve command by invalid command id from parameter`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { "INVALID" }
        val resolver = create()

        // act
        val result = resolver.resolve().toList()

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.size, 0)
        verify (exactly = 1) { _parametersServiceMock.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND) }
    }

    private fun create() =
        ParameterBasedDotnetCommandsStreamResolver(
            listOf(_dotnetCommandMock1, _dotnetCommandMock2, _dotnetCommandMock3),
            _parametersServiceMock,
        )
}