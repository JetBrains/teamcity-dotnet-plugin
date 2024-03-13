

package jetbrains.buildServer.dotnet.test.dotnet.commands

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.DotnetConstants.TEST_CASE_FILTER_REQUIREMENTS_MESSAGE
import jetbrains.buildServer.dotnet.commands.VSTestCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
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

class VSTestCommandTest {
    @MockK private lateinit var _toolStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _dotnetFilterFactory: DotnetFilterFactory
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _targetArgumentsProvider: TargetArgumentsProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _targetArgumentsProvider.getTargetArguments(any()) } answers { arg<Sequence<CommandTarget>>(0).map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) } }
        justRun { _loggerService.writeStandardOutput(any(), Color.Default) }
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(DotnetFilter("", null),
                        mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")),
                        listOf("vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("myfilter", null),
                        mapOf(
                                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                                DotnetConstants.PARAM_TEST_FILTER to "filter",
                                DotnetConstants.PARAM_PLATFORM to "x86",
                                DotnetConstants.PARAM_FRAMEWORK to "net45"),
                        listOf("/Settings:myconfig.txt", "/TestCaseFilter:myfilter", "/Platform:x86", "/Framework:net45", "vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("myfilter", File("genConfig.txt")),
                        mapOf(
                                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                                DotnetConstants.PARAM_TEST_FILTER to "filter",
                                DotnetConstants.PARAM_PLATFORM to "x86",
                                DotnetConstants.PARAM_FRAMEWORK to "net45"),
                        listOf("/Settings:genConfig.txt", "/TestCaseFilter:myfilter", "/Platform:x86", "/Framework:net45", "vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("myfilter", File("genConfig.txt")),
                        mapOf(
                                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                                DotnetConstants.PARAM_TEST_FILTER to "filter",
                                DotnetConstants.PARAM_PLATFORM to "x86",
                                DotnetConstants.PARAM_FRAMEWORK to "net45",
                                DotnetConstants.PARAM_TEST_CASE_FILTER to "myfilterAbc"),
                        listOf("/Settings:genConfig.txt", "/TestCaseFilter:myfilter", "/Platform:x86", "/Framework:net45", "vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("myfilter", null),
                        mapOf(
                                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                                DotnetConstants.PARAM_PLATFORM to "x86",
                                DotnetConstants.PARAM_FRAMEWORK to "net45"),
                        listOf("/Settings:myconfig.txt", "/TestCaseFilter:myfilter", "/Platform:x86", "/Framework:net45", "vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("", null),
                        mapOf(
                                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                                DotnetConstants.PARAM_TEST_FILTER to "name",
                                DotnetConstants.PARAM_PLATFORM to "x86",
                                DotnetConstants.PARAM_FRAMEWORK to "net45",
                                DotnetConstants.PARAM_TEST_NAMES to "mynames"),
                        listOf("/Settings:myconfig.txt", "/Tests:mynames", "/Platform:x86", "/Framework:net45", "vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("myfilter", null),
                        mapOf(
                                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                                DotnetConstants.PARAM_TEST_FILTER to "filter",
                                DotnetConstants.PARAM_PLATFORM to Platform.Default.id,
                                DotnetConstants.PARAM_FRAMEWORK to "net45"),
                        listOf("/Settings:myconfig.txt", "/TestCaseFilter:myfilter", "/Framework:net45", "vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("myfilter", null),
                        mapOf(
                                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                                DotnetConstants.PARAM_TEST_FILTER to "filter",
                                DotnetConstants.PARAM_PLATFORM to "x64",
                                DotnetConstants.PARAM_FRAMEWORK to "net45",
                                DotnetConstants.PARAM_VSTEST_IN_ISOLATION to "true"),
                        listOf("/Settings:myconfig.txt", "/TestCaseFilter:myfilter", "/Platform:x64", "/Framework:net45", "/InIsolation", "vstestlog", "customArg1")),
                arrayOf(
                        DotnetFilter("", null),
                        mapOf(DotnetConstants.PARAM_PATHS to "my.dll",
                                DotnetConstants.PARAM_TEST_FILTER to "name",
                                DotnetConstants.PARAM_TEST_NAMES to "test1 test2; test3"),
                        listOf("/Tests:test1,test2,test3", "vstestlog", "customArg1"))
        )
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
        filter: DotnetFilter,
        parameters: Map<String, String>,
        expectedArguments: List<String>) {
        // Given
        val command = createCommand(parameters = parameters, targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.VSTest }) } returns filter

        // When
        val actualArguments = command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command, Version.Empty, Verbosity.Detailed)).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }

    @DataProvider
    fun projectsArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("my.dll") as Any, listOf(listOf("my.dll"))),
                arrayOf(emptyList<String>() as Any, emptyList<List<String>>()),
                // https://youtrack.jetbrains.com/issue/TW-72213
                arrayOf(listOf("my.dll", "my2.dll") as Any, listOf(listOf("my.dll"), listOf("my2.dll"))))
    }

    @Test(dataProvider = "projectsArgumentsData")
    fun shouldProvideProjectsArguments(targets: List<String>, expectedArguments: List<List<String>>) {
        // Given
        val command = createCommand(targets = targets.asSequence())
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.VSTest }) } returns DotnetFilter("", null)

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
        Assert.assertEquals(actualCommand, DotnetCommandType.VSTest)
    }

    @Test
    fun shouldProvideToolExecutableFile() {
        // Given
        val command = createCommand()

        // When
        val actualExecutable = command.toolResolver.executable

        // Then
        Assert.assertEquals(actualExecutable, ToolPath(Path("vstest.console.exe")))
    }

    @Test
    fun shouldShowWarningWhenTestNamesFilterAndTestSpitting() {
        // Given
        val parameters = mapOf(
                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                DotnetConstants.PARAM_TEST_FILTER to "name",
                DotnetConstants.PARAM_PLATFORM to "x86",
                DotnetConstants.PARAM_FRAMEWORK to "net45",
                DotnetConstants.PARAM_TEST_NAMES to "mynames")

        val command = createCommand(parameters = parameters, targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.VSTest }) } returns DotnetFilter("not-empty-filter", null)
        every { _loggerService.writeWarning(any()) } returns Unit
        command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command, Version.Empty, Verbosity.Detailed)).map { it.value }.toList()

        // Then
        verify { _loggerService.writeWarning(any()) }
    }

    @Test
    fun shouldShowMessageWhenTestSpitting() {
        // Given
        val parameters = mapOf(
                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                DotnetConstants.PARAM_PLATFORM to "x86",
                DotnetConstants.PARAM_FRAMEWORK to "net45")

        val command = createCommand(parameters = parameters, targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.VSTest }) } returns DotnetFilter("splitting-filter", null)
        command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command, Version.Empty, Verbosity.Detailed)).map { it.value }.toList()

        // Then
        verify { _loggerService.writeStandardOutput(TEST_CASE_FILTER_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun shouldNotShowMessageWhenNoTestSpitting() {
        // Given
        val parameters = mapOf(
                DotnetConstants.PARAM_TEST_SETTINGS_FILE to "myconfig.txt",
                DotnetConstants.PARAM_PLATFORM to "x86",
                DotnetConstants.PARAM_FRAMEWORK to "net45")

        val command = createCommand(parameters = parameters, targets = sequenceOf("my.dll"), arguments = sequenceOf(CommandLineArgument("customArg1")))

        // When
        every { _dotnetFilterFactory.createFilter(match { it.command.commandType == DotnetCommandType.VSTest }) } returns DotnetFilter("", null)
        command.getArguments(DotnetCommandContext(ToolPath(Path("wd")), command, Version.Empty, Verbosity.Detailed)).map { it.value }.toList()

        // Then
        verify(inverse = true) { _loggerService.writeStandardOutput(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE) }
    }

    fun createCommand(
        parameters: Map<String, String> = emptyMap(),
        targets: Sequence<String> = emptySequence(),
        arguments: Sequence<CommandLineArgument> = emptySequence(),
        testsResultsAnalyzer: ResultsAnalyzer = TestsResultsAnalyzerStub()
    ): DotnetCommand {
        val ctx = Mockery()
        return VSTestCommand(
            ParametersServiceStub(parameters),
            testsResultsAnalyzer,
            TargetServiceStub(targets.map { CommandTarget(Path(it)) }.asSequence()),
            ArgumentsProviderStub(sequenceOf(CommandLineArgument("vstestlog"))),
            ArgumentsProviderStub(arguments),
            ToolResolverStub(ToolPlatform.Windows, ToolPath(Path("vstest.console.exe")), true, _toolStateWorkflowComposer),
            _dotnetFilterFactory,
            _loggerService,
            _targetArgumentsProvider,
            listOf(ctx.mock<EnvironmentBuilder>(EnvironmentBuilder::class.java))
        )
    }
}