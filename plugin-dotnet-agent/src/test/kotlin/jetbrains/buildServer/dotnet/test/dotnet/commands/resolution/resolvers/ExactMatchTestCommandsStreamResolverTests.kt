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

package jetbrains.buildServer.dotnet.test.dotnet.commands.resolution.resolvers

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.SplitTestsFilterSettings
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.resolvers.ExactMatchTestCommandsStreamResolver
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesSession
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesSessionManager
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ExactMatchTestCommandsStreamResolverTests {
    @MockK
    private lateinit var _splitTestsFilterSettingsMock: SplitTestsFilterSettings

    @MockK
    private lateinit var _listTestsDotnetCommandMock: DotnetCommand

    @MockK
    private lateinit var _testsNamesSessionManagerMock: SplitTestsNamesSessionManager

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should be on Transformation stage`() {
        // arrange
        val resolver = create()

        // act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsStreamResolvingStage.Transformation)
    }

    @Test
    fun `should not resolve if split tests disabled`() {
        // arrange
        every { _splitTestsFilterSettingsMock.isActive } answers { false }
        val commandMock = mockk<DotnetCommand>()
        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(commandMock)).toList()

        // assert
        Assert.assertSame(result.first(), commandMock)
    }

    @Test
    fun `should not resolve if exact match filter mode disabled`() {
        // arrange
        every { _splitTestsFilterSettingsMock.isActive } answers { true }
        every { _splitTestsFilterSettingsMock.useExactMatchFilter } answers { false }
        val commandMock = mockk<DotnetCommand>()
        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(commandMock)).toList()

        // assert
        Assert.assertSame(result.first(), commandMock)
    }

    @Test
    fun `should not resolve if there is no test command in stream`() {
        // arrange
        every { _splitTestsFilterSettingsMock.isActive } answers { true }
        every { _splitTestsFilterSettingsMock.useExactMatchFilter } answers { true }
        val commandMock = mockk<DotnetCommand>()
        every { commandMock.commandType } answers { DotnetCommandType.Build }
        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(commandMock)).toList()

        // assert
        Assert.assertSame(result.first(), commandMock)
    }

    @Test
    fun `should resolve by transforming test commands in stream`() {
        // arrange
        every { _splitTestsFilterSettingsMock.isActive } answers { true }
        every { _splitTestsFilterSettingsMock.useExactMatchFilter } answers { true }

        val buildCommandMock = mockk<DotnetCommand>()
        every { buildCommandMock.commandType } answers { DotnetCommandType.Build }

        val testCommandMock = mockk<DotnetCommand>()
        every { testCommandMock.commandType } answers { DotnetCommandType.Test }
        every { testCommandMock.targetArguments } answers { mockk() }

        val sessionMock = mockk<SplitTestsNamesSession>()
        every<Sequence<DotnetCommand>> {
            sessionMock.forEveryTestsNamesChunk(any())
        } answers { sequenceOf(testCommandMock, testCommandMock) }
        justRun { sessionMock.dispose() }

        every { _testsNamesSessionManagerMock.startSession() } answers { sessionMock }

        every { _listTestsDotnetCommandMock.commandType } answers { DotnetCommandType.ListTests}

        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(buildCommandMock, testCommandMock)).toList()

        // assert
        Assert.assertEquals(result.size, 4)
        Assert.assertSame(result[0], buildCommandMock)
        Assert.assertEquals(result[1].commandType, DotnetCommandType.ListTests)
        Assert.assertSame(result[2], testCommandMock)
        Assert.assertSame(result[3], testCommandMock)
    }

    @Test
    fun `should listen tests list output`() {
        // arrange
        every { _splitTestsFilterSettingsMock.isActive } answers { true }
        every { _splitTestsFilterSettingsMock.useExactMatchFilter } answers { true }

        val testCommandMock = mockk<DotnetCommand>()
        every { testCommandMock.commandType } answers { DotnetCommandType.Test }
        every { testCommandMock.targetArguments } answers { mockk() }

        val sessionMock = mockk<SplitTestsNamesSession>()
        every<Sequence<DotnetCommand>> {
            sessionMock.forEveryTestsNamesChunk(any())
        } answers { sequenceOf(testCommandMock) }
        justRun { sessionMock.dispose() }
        justRun { sessionMock.tryToSave(any()) }

        every { _testsNamesSessionManagerMock.startSession() } answers { sessionMock }

        val result = create().resolve(sequenceOf(testCommandMock)).toList()
        val resultObserver = result[0].resultsObserver


        val markerOutputMock = mockk<CommandResultOutput>()
        every { markerOutputMock.output } answers { "   The following Tests are available:          " }

        val attributesOfMarkerOutput = mutableSetOf<CommandResultAttribute>()
        every { markerOutputMock.attributes } answers { attributesOfMarkerOutput }

        val (testName0, testName1) = Pair("Namespace.TestClass.TestName0", "Namespace.TestClass.TestName1")
        val resultLine = "   $testName0          $testName1    "
        val attributesOfTestNamesOutput = mutableSetOf<CommandResultAttribute>()
        val testNamesOutputMock = mockk<CommandResultOutput>()
        every { testNamesOutputMock.output } answers { resultLine }
        every { testNamesOutputMock.attributes } answers { attributesOfTestNamesOutput }

        // act
        resultObserver.onNext(mockk())
        resultObserver.onNext(markerOutputMock)
        resultObserver.onNext(testNamesOutputMock)

        // assert
        verify (exactly = 1) { sessionMock.tryToSave(testName0) }
        verify (exactly = 1) { sessionMock.tryToSave(testName1) }
        Assert.assertEquals(attributesOfMarkerOutput.first(), CommandResultAttribute.Suppressed)
        Assert.assertEquals(attributesOfTestNamesOutput.first(), CommandResultAttribute.Suppressed)
    }

    private fun create() = ExactMatchTestCommandsStreamResolver(
        _splitTestsFilterSettingsMock, _listTestsDotnetCommandMock, _testsNamesSessionManagerMock)
}