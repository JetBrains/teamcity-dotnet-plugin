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
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class BasePathResolverWorkflowComposerTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _workflowContext: WorkflowContext
    private val _pathEvents = mutableListOf<Notification<Path>>()
    private val _commandSubject = subjectOf<CommandResultEvent>()
    private val _workingDirectory = "wd"

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        _pathEvents.clear()

        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File(_workingDirectory)
        every { _workflowContext.subscribe(any()) } answers { _commandSubject.subscribe(arg<Observer<CommandResultEvent>>(0)) }
    }

    @Test
    fun shouldProvideCommandToResolvePath() {
        // Given
        val composer = createInstance()
        val state = PathResolverState(Path("dotnet"), _pathEvents.toObserver().dematerialize(), Path("where"))

        // When
        var actualCommandLines = composer.compose(_workflowContext, state).commandLines.toList()

        // Then
        Assert.assertEquals(
                actualCommandLines,
                listOf(CommandLine(
                        null,
                        TargetType.SystemDiagnostics,
                        Path("where"),
                        Path(_workingDirectory),
                        listOf(CommandLineArgument("dotnet", CommandLineArgumentType.Target)),
                        emptyList(),
                        "get dotnet")))
    }

    @DataProvider(name = "paths")
    fun paths(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        sequenceOf<CommandResultEvent>(
                                CommandResultOutput("Access is denied"),
                                CommandResultOutput("abc/dotnet"),
                                CommandResultOutput("abc/dotnet"),
                                CommandResultOutput(""),
                                CommandResultOutput("sxs"),
                                CommandResultOutput("xyz/dotnet"),
                                CommandResultOutput("dotnet")),
                        listOf(
                                NotificationNext<Path>(Path("abc/dotnet")),
                                NotificationNext<Path>(Path("xyz/dotnet")),
                                NotificationNext<Path>(Path("dotnet")),
                                NotificationCompleted.completed<Path>())))
    }

    @Test(dataProvider = "paths")
    fun shouldResolvePath(output: Sequence<CommandResultEvent>, expectedPaths: List<NotificationNext<Path>>) {
        // Given
        val composer = createInstance()
        val state = PathResolverState(Path("dotnet"), _pathEvents.toObserver().dematerialize(), Path("where"))

        // When
        val iterator = composer.compose(_workflowContext, state).commandLines.iterator()
        iterator.hasNext()
        output.toObservable().subscribe(_commandSubject);
        iterator.hasNext()

        // Then
        Assert.assertEquals(_pathEvents, expectedPaths)
    }

    private fun createInstance() = BasePathResolverWorkflowComposer(_pathsService, _virtualContext)
}