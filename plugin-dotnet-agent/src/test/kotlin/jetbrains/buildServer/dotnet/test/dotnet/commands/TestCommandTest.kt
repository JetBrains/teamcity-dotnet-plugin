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

package jetbrains.buildServer.dotnet.test.dotnet.commands

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.TestCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
import jetbrains.buildServer.dotnet.commands.targeting.TargetTypeProvider
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.dotnet.test.dotnet.ArgumentsProviderStub
import jetbrains.buildServer.dotnet.test.dotnet.commands.targeting.TargetServiceStub
import jetbrains.buildServer.dotnet.test.dotnet.commands.test.TestsResultsAnalyzerStub
import jetbrains.buildServer.dotnet.test.dotnet.toolResolvers.ToolResolverStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestCommandTest {
    @MockK private lateinit var _toolStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _dotnetFilterFactory: DotnetFilterFactory
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _targetTypeProvider: TargetTypeProvider
    @MockK private lateinit var _targetArgumentsProvider: TargetArgumentsProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _targetArgumentsProvider.getTargetArguments(any()) } answers { arg<Sequence<CommandTarget>>(0).map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) } }
        every { _targetTypeProvider.getTargetType(any()) } answers { if("dll".equals(arg<File>(0).extension, true)) CommandTargetType.Assembly else CommandTargetType.Unknown }
        every { _loggerService.writeStandardOutput(DotnetConstants.PARALLEL_TESTS_FEATURE_REQUIREMENTS_MESSAGE) } returns Unit
    }

    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        DotnetFilter("", null, false),
                        listOf("customArg1")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_CONFIG, "Release")),
                        DotnetFilter("", null, false),
                        listOf("--framework", "dotcore", "--configuration", "Release", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                        DotnetFilter("", null, false),
                        listOf("--no-build", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_OUTPUT_DIR, "out")),
                        DotnetFilter("", null, false),
                        listOf("--output", "out", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")),
                        DotnetFilter("myFilter", null, false),
                        listOf("--filter", "myFilter", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_SETTINGS_FILE, "User.settings")),
                        DotnetFilter("myFilter", null, false),
                        listOf("--filter", "myFilter", "--settings", "User.settings", "customArg1")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter"), Pair(DotnetConstants.PARAM_TEST_SETTINGS_FILE, "User.settings")),
                        DotnetFilter("myFilter", File("Abc.settings"), false),
                        listOf("--filter", "myFilter", "--settings", "Abc.settings", "customArg1"))
        )
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            filter: DotnetFilter,
            expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))
        every { _dotnetFilterFactory.createFilter(DotnetCommandType.Test) } returns filter

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
        every { _dotnetFilterFactory.createFilter(DotnetCommandType.Test) } returns DotnetFilter("", null, false)

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

    @Test
    fun shouldShowMessageWhenTestSpitting() {
        // Given
        val command = createCommand(targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        every { _dotnetFilterFactory.createFilter(DotnetCommandType.Test) } returns DotnetFilter("", null, true)
        command.getArguments(DotnetBuildContext(ToolPath(Path("wd")), command, Version(1, 1), Verbosity.Detailed)).map { it.value }.toList()

        // Then
        verify { _loggerService.writeStandardOutput(DotnetConstants.PARALLEL_TESTS_FEATURE_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun shouldNotShowMessageWhenNoTestSpitting() {
        // Given
        val command = createCommand(targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        every { _dotnetFilterFactory.createFilter(DotnetCommandType.Test) } returns DotnetFilter("", null, false)
        command.getArguments(DotnetBuildContext(ToolPath(Path("wd")), command, Version(1, 1), Verbosity.Detailed)).map { it.value }.toList()

        // Then
        verify(inverse = true) { _loggerService.writeStandardOutput(DotnetConstants.PARALLEL_TESTS_FEATURE_REQUIREMENTS_MESSAGE) }
    }

    fun createCommand(
            parameters: Map<String, String> = emptyMap(),
            targets: Sequence<String> = emptySequence(),
            arguments: Sequence<CommandLineArgument> = emptySequence(),
            testsResultsAnalyzer: ResultsAnalyzer = TestsResultsAnalyzerStub()
    ): DotnetCommand {
        return TestCommand(
            ParametersServiceStub(parameters),
            testsResultsAnalyzer,
            ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")), true, _toolStateWorkflowComposer),
            TargetServiceStub(targets.map { CommandTarget(Path(it)) }.asSequence()),
            ArgumentsProviderStub(arguments),
            ArgumentsProviderStub(arguments),
            _dotnetFilterFactory,
            _loggerService,
            _targetTypeProvider,
            _targetArgumentsProvider
        )
    }
}