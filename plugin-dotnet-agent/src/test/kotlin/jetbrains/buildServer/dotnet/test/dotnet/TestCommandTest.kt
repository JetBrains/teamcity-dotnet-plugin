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
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestCommandTest {
    @MockK private lateinit var _toolStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _argumentsAlternative: ArgumentsAlternative
    @MockK private lateinit var _testsFilterProvider: TestsFilterProvider
    @MockK private lateinit var _targetTypeProvider: TargetTypeProvider
    @MockK private lateinit var _targetArgumentsProvider: TargetArgumentsProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _targetArgumentsProvider.getTargetArguments(any()) } answers { arg<Sequence<CommandTarget>>(0).map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) } }
        every { _targetTypeProvider.getTargetType(any()) } answers { if("dll".equals(arg<File>(0).extension, true)) CommandTargetType.Assembly else CommandTargetType.Unknown }
    }

    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        "",
                        listOf("customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_CONFIG, "Release")),
                        "",
                        listOf("--framework", "dotcore", "--configuration", "Release", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                        "",
                        listOf("--no-build", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_OUTPUT_DIR, "out")),
                        "",
                        listOf("--output", "out", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")),
                        "myFilter",
                        listOf("@filterRsp", "customArg1")))
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            testsFilter: String,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))
        every { _testsFilterProvider.filterExpression } returns testsFilter
        every {
            _argumentsAlternative.select(
                    "Filter",
                    listOf(
                            CommandLineArgument("--filter"),
                            CommandLineArgument(testsFilter)
                    ),
                    emptySequence(),
                    match { it.toList().equals(listOf(MSBuildParameter("VSTestTestCaseFilter", testsFilter))) },
                    Verbosity.Detailed
            )
        } returns sequenceOf(CommandLineArgument("@filterRsp"))

        // When
        val actualArguments = command.getArguments(DotnetBuildContext(ToolPath(Path("wd")), command, Version(1), Verbosity.Detailed)).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<List<Any>>> {
        return arrayOf(
                arrayOf(listOf("my.csproj"), listOf(listOf("my.csproj"))),
                arrayOf(emptyList<String>(), emptyList<List<String>>()),
                arrayOf(listOf("my.csproj", "my2.csproj"), listOf(listOf("my.csproj"), listOf("my2.csproj"))),
                // https://youtrack.jetbrains.com/issue/TW-72213
                arrayOf(listOf("my.csproj", "my2.dll", "my3.dll"), listOf(listOf("my.csproj"), listOf("my2.dll"), listOf("my3.dll"))),
                // https://youtrack.jetbrains.com/issue/TW-72213
                arrayOf(listOf("my.csproj", "my2.dll", "my3.dll", "my4.Sln", "my5.dll"), listOf(listOf("my.csproj"), listOf("my2.dll"), listOf("my3.dll"), listOf("my4.Sln"), listOf("my5.dll"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val command = createCommand(targets = targets.asSequence())

        // When
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun shouldProvideCommandType() {
        // Given
        val command = createCommand()

        // When
        val actualCommand = command.commandType

        // Then
        Assert.assertEquals(actualCommand, DotnetCommandType.Test)
    }

    @DataProvider
    fun testFilterData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("my.csproj"), mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")), listOf("@filterRsp", "customArg1")),
                arrayOf(listOf("my.dll"), mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")), listOf("--filter", "myFilter", "customArg1")),
                arrayOf(listOf("my.csproj", "abc/my.DlL"), mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")), listOf("--filter", "myFilter", "customArg1")))
    }

    @Test(dataProvider = "testFilterData")
    fun shouldProvideFilter(
            targets: List<String>,
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, targets = targets.asSequence(), arguments = sequenceOf(CommandLineArgument("customArg1")))
        every { _testsFilterProvider.filterExpression } returns "myFilter"
        every {
            _argumentsAlternative.select(
                    "Filter",
                    listOf(
                            CommandLineArgument("--filter"),
                            CommandLineArgument("myFilter")
                    ),
                    emptySequence(),
                    match { it.toList().equals(listOf(MSBuildParameter("VSTestTestCaseFilter", "myFilter"))) },
                    Verbosity.Detailed
            )
        } returns sequenceOf(CommandLineArgument("@filterRsp"))

        // When
        val actualArguments = command.getArguments(DotnetBuildContext(ToolPath(Path("wd")), command, Version(1), Verbosity.Detailed)).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence(),
            testsResultsAnalyzer: ResultsAnalyzer = TestsResultsAnalyzerStub()): DotnetCommand {
        return TestCommand(
                ParametersServiceStub(parameters),
                testsResultsAnalyzer,
                TargetServiceStub(targets.map { CommandTarget(Path(it)) }.asSequence()),
                ArgumentsProviderStub(arguments),
                ArgumentsProviderStub(arguments),
                ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")), true, _toolStateWorkflowComposer),
                mockk<EnvironmentBuilder>(),
                _argumentsAlternative,
                _testsFilterProvider,
                _targetTypeProvider,
                _targetArgumentsProvider)
    }
}