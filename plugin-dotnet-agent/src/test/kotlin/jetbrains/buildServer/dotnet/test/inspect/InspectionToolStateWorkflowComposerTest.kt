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

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.WorkflowContext
import jetbrains.buildServer.inspect.*
import jetbrains.buildServer.rx.*
import org.testng.annotations.BeforeMethod

import org.testng.Assert.*
import org.testng.annotations.AfterMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class InspectionToolStateWorkflowComposerTest {
    @MockK
    private lateinit var _pathsService: PathsService

    @MockK
    private lateinit var _versionParser: ToolVersionOutputParser

    @MockK
    private lateinit var _context: WorkflowContext

    private val _versionSubject = subjectOf<Version>()
    private val _actualObservedVersions = mutableListOf<Version>()
    private var _disposableVersionSubject: Disposable = emptyDisposable()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        _actualObservedVersions.clear()
        _disposableVersionSubject = disposableOf(
            _versionSubject.subscribe { _actualObservedVersions.add(it) },
        )
    }

    @AfterMethod
    fun teardown() {
        _disposableVersionSubject.dispose()
    }

    @DataProvider
    fun composeCases(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf(InspectionTool.Inspectcode, listOf(Version.Empty, Version(2018, 9, 2), Version.Empty), listOf(Version(2018, 9, 2))),
            arrayOf(InspectionTool.Dupfinder, listOf(Version.Empty, Version(2018, 9, 2), Version.Empty), listOf(Version(2018, 9, 2))),
            arrayOf(InspectionTool.Inspectcode, listOf(Version.Empty, Version.Empty), emptyList<Version>()),
            arrayOf(InspectionTool.Inspectcode, emptyList<Version>(), emptyList<Version>()),
        )
    }

    @Test(dataProvider = "composeCases")
    fun `should compose tool version command line and observe version when parsed`(
        toolType: InspectionTool,
        outputParsedVersions: List<Version>,
        expectedObservedVersions: List<Version>
    ) {
        // arrange
        val composer = createInstance(toolType)
        val workingDirectoryPath = File("/working/directory")
        val executablePath = Path("/path/to/tool.sh")
        val startArguments = listOf(CommandLineArgument("exec"), CommandLineArgument("/some/executable.exe"))
        val toolStartCommand = ToolStartCommand(executablePath, startArguments)
        every { _context.subscribe(any()) } answers {
            val observer = arg<Observer<CommandResultEvent>>(0)
            outputParsedVersions.forEach {
                val outputLine = if (!it.isEmpty()) it.toString() else "line that does not contain version"
                observer.onNext(CommandResultOutput(outputLine))
            }
            emptyDisposable()
        }
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns workingDirectoryPath
        every { _versionParser.parse(any()) } answers { Version.parseSimplified(arg<List<String>>(0)[0]) }
        val toolState = InspectionToolState(toolStartCommand, _versionSubject)

        // act
        val commandLines = composer.compose(_context, toolState).commandLines.toList()

        // assert
        assertEquals(
            commandLines,
            listOf(
                CommandLine(
                    baseCommandLine = null,
                    target = TargetType.SystemDiagnostics,
                    executableFile = executablePath,
                    workingDirectory = Path(workingDirectoryPath.canonicalPath),
                    arguments = startArguments.plus(CommandLineArgument("--version")),
                    environmentVariables = emptyList(),
                    title = "Getting ${toolType.displayName} version"
                )
            )
        )
        verify(exactly = 1) { _context.subscribe(any()) }
        assertEquals(_actualObservedVersions, expectedObservedVersions)
    }

    private fun createInstance(toolType: InspectionTool) = InspectionToolStateWorkflowComposerImpl(toolType, _pathsService, _versionParser)
}