

package jetbrains.buildServer.dotnet.test.dotnet.commands

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
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
import org.jmock.Mockery
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
        every { _targetArgumentsProvider.getTargetArguments(any()) } answers {
            arg<Sequence<CommandTarget>>(0)
                .map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }
        }
        every { _targetTypeProvider.getTargetType(any()) } answers {
            if("dll".equals(arg<File>(0).extension, true)) CommandTargetType.Assembly else CommandTargetType.Unknown
        }
        every { _loggerService.writeStandardOutput(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) } returns Unit
    }

    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                DotnetFilter("", null),
                listOf("customArg1")),
            arrayOf(mapOf(
                Pair(DotnetConstants.PARAM_FRAMEWORK, "dotcore"),
                Pair(DotnetConstants.PARAM_CONFIG, "Release")),
                DotnetFilter("", null),
                listOf("--framework", "dotcore", "--configuration", "Release", "customArg1")),
            arrayOf(mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                DotnetFilter("", null),
                listOf("--no-build", "customArg1")),
            arrayOf(mapOf(Pair(DotnetConstants.PARAM_OUTPUT_DIR, "out")),
                DotnetFilter("", null),
                listOf("--output", "out", "customArg1")),
            arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter")),
                DotnetFilter("myFilter", null),
                listOf("--filter", "myFilter", "customArg1")),
            arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_SETTINGS_FILE, "User.settings")),
                DotnetFilter("myFilter", null),
                listOf("--filter", "myFilter", "--settings", "User.settings", "customArg1")),
            arrayOf(mapOf(Pair(DotnetConstants.PARAM_TEST_CASE_FILTER, "filter"), Pair(DotnetConstants.PARAM_TEST_SETTINGS_FILE, "User.settings")),
                DotnetFilter("myFilter", File("Abc.settings")),
                listOf("--filter", "myFilter", "--settings", "Abc.settings", "customArg1"))
        )
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun `should get arguments`(
        parameters: Map<String, String>,
        filter: DotnetFilter,
        expectedArguments: List<String>
    ) {
        // arrange
        val command = createCommand(parameters = parameters, targets = sequenceOf("my.csproj"), arguments = sequenceOf(CommandLineArgument("customArg1")))
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.Test }) } returns filter

        // act
        val actualArguments = command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command, Version(1), Verbosity.Detailed)).map { it.value }.toList()

        // assert
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun noBuildArgumentsData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                emptyMap<String, String>(),
                sequenceOf("my.csproj"),
                emptyList<String>()
            ),
            arrayOf(
                mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                emptySequence<String>(),
                listOf("--no-build")
            ),
            arrayOf(
                mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                sequenceOf("my.csproj"),
                listOf("--no-build")
            ),
            arrayOf(
                mapOf(Pair(DotnetConstants.PARAM_SKIP_BUILD, "true")),
                sequenceOf("my1.dll", "my2.dll", "my3.dll"),
                emptyList<String>()
            )
        )
    }

    @Test(dataProvider = "noBuildArgumentsData")
    fun `should provide no-build argument`(parameters: Map<String, String>, targets: Sequence<String>, expectedArguments: List<String>) {
        // arrange
        val command = createCommand(parameters, targets)
        every { _dotnetFilterFactory.createFilter(any()) } returns DotnetFilter("", null)

        // act
        val actualArguments = command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command)).map { it.value }.toList()

        // assert
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
    fun `should provide projects arguments`(targets: List<String>, expectedArguments: List<List<String>>) {
        // arrange
        val command = createCommand(targets = targets.asSequence())
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.Test }) } returns DotnetFilter("", null)

        // act
        val actualArguments = command.targetArguments.map { it.arguments.map { it.value }.toList() }.toList()

        // assert
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @Test
    fun `should provide command type`() {
        // arrange
        val command = createCommand()

        // act
        val actualCommand = command.commandType

        // assert
        Assert.assertEquals(actualCommand, DotnetCommandType.Test)
    }

    @Test
    fun `should not show message when no test spitting`() {
        // arrange
        val command = createCommand(targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // act
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.Test }) } returns DotnetFilter("", null)
        command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command, Version(1, 1), Verbosity.Detailed)).map { it.value }.toList()

        // assert
        verify(inverse = true) { _loggerService.writeStandardOutput(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }

    fun createCommand(
        parameters: Map<String, String> = emptyMap(),
        targets: Sequence<String> = emptySequence(),
        arguments: Sequence<CommandLineArgument> = emptySequence(),
        testsResultsAnalyzer: ResultsAnalyzer = TestsResultsAnalyzerStub()
    ): DotnetCommand {
        val ctx = Mockery()
        return TestCommand(
            ParametersServiceStub(parameters),
            testsResultsAnalyzer,
            ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path("dotnet")), true, _toolStateWorkflowComposer),
            TargetServiceStub(targets.map { CommandTarget(Path(it)) }.asSequence()),
            ArgumentsProviderStub(arguments),
            ArgumentsProviderStub(arguments),
            _dotnetFilterFactory,
            _targetTypeProvider,
            _targetArgumentsProvider,
            listOf(ctx.mock<EnvironmentBuilder>(EnvironmentBuilder::class.java))
        )
    }
}