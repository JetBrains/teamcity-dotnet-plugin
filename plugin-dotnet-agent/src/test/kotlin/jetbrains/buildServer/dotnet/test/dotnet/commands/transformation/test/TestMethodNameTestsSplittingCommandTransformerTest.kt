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

package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation.test

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesSession
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesSessionManager
import jetbrains.buildServer.dotnet.commands.transformation.test.TestMethodNameFilterTestSplittingCommandTransformer
import jetbrains.buildServer.rx.Disposable
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestMethodNameTestsSplittingCommandTransformerTest {
    @MockK private lateinit var _listTestsDotnetCommandMock: DotnetCommand
    @MockK private lateinit var _testsNamesSessionManagerMock: TestsSplittingByNamesSessionManager
    @MockK private lateinit var _loggerServiceMock: LoggerService

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        justRun { _loggerServiceMock.writeTrace(any()) }
        every { _loggerServiceMock.writeBlock(any()) } returns mockk<Disposable> {
            justRun { dispose() }
        }
    }

    @Test
    fun `should transform test command in stream`() {
        // arrange
        val testCommandMock = mockk<DotnetCommand>()
        every { testCommandMock.commandType } answers { DotnetCommandType.Test }
        every { testCommandMock.targetArguments } answers { mockk() }

        val sessionMock = mockk<TestsSplittingByNamesSession>()
        every<Sequence<DotnetCommand>> {
            sessionMock.forEveryTestsNamesChunk(any())
        } answers { sequenceOf(testCommandMock, testCommandMock) }
        justRun { sessionMock.dispose() }

        every { _testsNamesSessionManagerMock.startSession() } answers { sessionMock }

        every { _listTestsDotnetCommandMock.commandType } answers { DotnetCommandType.ListTests}

        val transformer = create()

        // act
        val result = transformer.transform(testCommandMock).toList()

        // assert
        Assert.assertEquals(result.size, 3)
        Assert.assertEquals(result[0].commandType, DotnetCommandType.ListTests)
        Assert.assertSame(result[1], testCommandMock)
        Assert.assertSame(result[2], testCommandMock)
        verify(exactly = 1) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = 1) { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun `should listen tests list output`() {
        // arrange
        val testCommandMock = mockk<DotnetCommand>()
        every { testCommandMock.commandType } answers { DotnetCommandType.Test }
        every { testCommandMock.targetArguments } answers { mockk() }

        val sessionMock = mockk<TestsSplittingByNamesSession>()
        every<Sequence<DotnetCommand>> {
            sessionMock.forEveryTestsNamesChunk(any())
        } answers { sequenceOf(testCommandMock) }
        justRun { sessionMock.dispose() }
        justRun { sessionMock.tryToSave(any()) }

        every { _testsNamesSessionManagerMock.startSession() } answers { sessionMock }

        val result = create().transform(testCommandMock).toList()
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
        verify(exactly = 1) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = 1) { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }

    private fun create() = TestMethodNameFilterTestSplittingCommandTransformer(
        _listTestsDotnetCommandMock,
        _testsNamesSessionManagerMock,
        _loggerServiceMock
    )
}