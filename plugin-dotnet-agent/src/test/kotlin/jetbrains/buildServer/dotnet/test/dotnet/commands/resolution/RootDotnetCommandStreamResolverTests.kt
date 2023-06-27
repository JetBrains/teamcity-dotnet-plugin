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

package jetbrains.buildServer.dotnet.test.dotnet.commands.resolution

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolver
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.RootDotnetCommandResolver
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class RootDotnetCommandStreamResolverTests {
    @MockK
    private lateinit var _resolverMock1: DotnetCommandsResolver

    @MockK
    private lateinit var _resolverMock2: DotnetCommandsResolver

    @MockK
    private lateinit var _resolverMock3: DotnetCommandsResolver

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should be on initial stage`() {
        // arrange
        val resolver = create()

        // act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsResolvingStage.Initial)
    }

    @Test
    fun `should be always applicable`() {
        // arrange
        every { _resolverMock1.stage } answers { DotnetCommandsResolvingStage.CommandRetrieve }
        every { _resolverMock2.stage } answers { DotnetCommandsResolvingStage.Targeting }
        every { _resolverMock3.stage } answers { DotnetCommandsResolvingStage.Transformation }

        val resolver1ResultStream = mockk<DotnetCommandsStream>()
        every { _resolverMock1.resolve(any()) } answers { resolver1ResultStream }
        val resolver2ResultStream = mockk<DotnetCommandsStream>()
        every { _resolverMock2.resolve(any()) } answers { resolver2ResultStream }
        val resolver3ResultStream = mockk<DotnetCommandsStream>()
        every { _resolverMock3.resolve(any()) } answers { resolver3ResultStream }

        val resolver = create()

        val initialStream = mockk<DotnetCommandsStream>()

        // act
        val result = resolver.resolve(initialStream)

        // assert
        Assert.assertNotNull(result)
        verify (exactly = 1) { _resolverMock1.resolve(match { it == initialStream }) }
        verify (exactly = 1) { _resolverMock2.resolve(match { it == resolver1ResultStream }) }
        verify (exactly = 1) { _resolverMock3.resolve(match { it == resolver2ResultStream }) }
        Assert.assertEquals(result, resolver3ResultStream)
    }

    private fun create() = RootDotnetCommandResolver(listOf(_resolverMock1, _resolverMock2, _resolverMock3))
}