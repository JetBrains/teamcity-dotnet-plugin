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

package jetbrains.buildServer.dotnet.test.dotnet.commands.resolution.resolvers.transformation

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.resolvers.transformation.TestMethodNameFilterTestSplittingCommandsResolver
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesSession
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesSessionManager
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.rx.Disposable
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestMethodNameTestsSplittingCommandsResolverTest {
    @MockK private lateinit var _testsSplittingSettingsMock: TestsSplittingSettings
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
    fun `should be on Transformation stage`() {
        // arrange
        val resolver = create()

        // act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsResolvingStage.Transformation)
    }

    @Test
    fun `should not resolve if split tests disabled`() {
        // arrange
        every { _testsSplittingSettingsMock.mode } answers { TestsSplittingMode.Disabled }
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
        every { _testsSplittingSettingsMock.mode } answers { TestsSplittingMode.TestClassNameFilter }
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
        every { _testsSplittingSettingsMock.mode } answers { TestsSplittingMode.TestNameFilter }
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
        every { _testsSplittingSettingsMock.mode } answers { TestsSplittingMode.TestNameFilter }

        val buildCommandMock = mockk<DotnetCommand>()
        every { buildCommandMock.commandType } answers { DotnetCommandType.Build }

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

        val resolver = create()

        // act
        val result = resolver.resolve(sequenceOf(buildCommandMock, testCommandMock)).toList()

        // assert
        Assert.assertEquals(result.size, 4)
        Assert.assertSame(result[0], buildCommandMock)
        Assert.assertEquals(result[1].commandType, DotnetCommandType.ListTests)
        Assert.assertSame(result[2], testCommandMock)
        Assert.assertSame(result[3], testCommandMock)
        verify(exactly = 1) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = 1) { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun `should listen tests list output`() {
        // arrange
        every { _testsSplittingSettingsMock.mode } answers { TestsSplittingMode.TestNameFilter }

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
        verify(exactly = 1) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = 1) { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }

    private fun create() = TestMethodNameFilterTestSplittingCommandsResolver(
        _testsSplittingSettingsMock,
        _listTestsDotnetCommandMock,
        _testsNamesSessionManagerMock,
        _loggerServiceMock
    )
}