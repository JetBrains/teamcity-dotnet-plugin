package jetbrains.buildServer.dotnet.test.visualStudio

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TargetService
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.visualStudio.ToolResolver
import jetbrains.buildServer.visualStudio.VisualStudioWorkflowComposer
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioWorkflowComposerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _targetService: TargetService
    private lateinit var _toolResolver: ToolResolver
    private lateinit var _loggerService: LoggerService
    private lateinit var _targetRegistry: TargetRegistry
    private lateinit var _targetRegistrationToken: Disposable

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock(PathsService::class.java)
        _targetService = _ctx.mock(TargetService::class.java)
        _toolResolver = _ctx.mock(ToolResolver::class.java)
        _loggerService = _ctx.mock(LoggerService::class.java)
        _targetRegistry = _ctx.mock(TargetRegistry::class.java)
        _targetRegistrationToken = _ctx.mock(Disposable::class.java)
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_COMMAND to DotnetCommandType.VisualStudio.id,
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "build",
                                DotnetConstants.PARAM_CONFIG to "Debug",
                                DotnetConstants.PARAM_PLATFORM to "x86",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1 arg2"),
                        sequenceOf(CommandTarget(File("my1.sln")), CommandTarget(File("my2.sln"))),
                        listOf(
                                CommandLine(
                                        TargetType.Tool,
                                        File("tool"),
                                        File("wd"),
                                        listOf(
                                                CommandLineArgument(File("my1.sln").absolutePath),
                                                CommandLineArgument("/build"),
                                                CommandLineArgument("\"Debug|x86\""),
                                                CommandLineArgument("arg1"),
                                                CommandLineArgument("arg2")),
                                        emptyList()),
                                CommandLine(
                                        TargetType.Tool,
                                        File("tool"),
                                        File("wd"),
                                        listOf(
                                                CommandLineArgument(File("my2.sln").absolutePath),
                                                CommandLineArgument("/build"),
                                                CommandLineArgument("\"Debug|x86\""),
                                                CommandLineArgument("arg1"),
                                                CommandLineArgument("arg2")),
                                        emptyList()))),
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_COMMAND to DotnetCommandType.VisualStudio.id,
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "rebuild",
                                DotnetConstants.PARAM_CONFIG to "release",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1"),
                        sequenceOf(CommandTarget(File("my1.csproj"))),
                        listOf(
                                CommandLine(
                                        TargetType.Tool,
                                        File("tool"),
                                        File("wd"),
                                        listOf(
                                                CommandLineArgument(File("my1.csproj").absolutePath),
                                                CommandLineArgument("/rebuild"),
                                                CommandLineArgument("release"),
                                                CommandLineArgument("arg1")),
                                        emptyList()))),
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_COMMAND to "abc",
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "rebuild",
                                DotnetConstants.PARAM_CONFIG to "release",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1"),
                        sequenceOf(CommandTarget(File("my1.csproj"))),
                        emptyList<CommandLine>()),
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "rebuild",
                                DotnetConstants.PARAM_CONFIG to "release",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1"),
                        sequenceOf(CommandTarget(File("my1.csproj"))),
                        emptyList<CommandLine>()))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            parameters: Map<String, String>,
            targets: Sequence<CommandTarget>,
            expectedCommandLines: List<CommandLine>) {
        // Given

        val workingDirectory = File("wd")
        val composer = createInstance(parameters)

        // When
        _ctx.checking(object : Expectations() {
            init {
                allowing<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                allowing<ToolResolver>(_toolResolver).executableFile
                will(returnValue(File("tool")))

                allowing<TargetService>(_targetService).targets
                will(returnValue(targets))

                allowing<TargetRegistry>(_targetRegistry).register(TargetType.Tool)
                will(returnValue(_targetRegistrationToken))

                allowing<Disposable>(_targetRegistrationToken).dispose()
            }
        })

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0))).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedCommandLines)
    }

    @DataProvider(name = "abortCases")
    fun getAbortCases(): Array<Array<Int>> {
        return arrayOf(arrayOf(1), arrayOf(-1), arrayOf(10000))
    }

    @Test(dataProvider = "abortCases")
    fun shouldAbortWhenStepFailed(exitCode: Int) {
        // Given
        val parameters = mapOf(
                DotnetConstants.PARAM_COMMAND to DotnetCommandType.VisualStudio.id,
                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "build",
                DotnetConstants.PARAM_CONFIG to "Debug",
                DotnetConstants.PARAM_PLATFORM to "x86",
                DotnetConstants.PARAM_ARGUMENTS to "arg1 arg2")

        val targets = sequenceOf(CommandTarget(File("my1.sln")), CommandTarget(File("my2.sln")))
        val expectedCommandLines = listOf(
                CommandLine(
                        TargetType.Tool,
                        File("tool"),
                        File("wDir"),
                        listOf(
                                CommandLineArgument(File("my1.sln").absolutePath),
                                CommandLineArgument("/build"),
                                CommandLineArgument("\"Debug|x86\""),
                                CommandLineArgument("arg1"),
                                CommandLineArgument("arg2")),
                        emptyList()))

        val workingDirectory = File("wDir")
        val composer = createInstance(parameters)

        // When
        _ctx.checking(object : Expectations() {
            init {
                allowing<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                allowing<ToolResolver>(_toolResolver).executableFile
                will(returnValue(File("tool")))

                allowing<TargetService>(_targetService).targets
                will(returnValue(targets))

                oneOf<LoggerService>(_loggerService).writeBuildProblem(BuildProblemData.createBuildProblem("visual_studio_exit_code$exitCode", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code $exitCode"))

                oneOf<TargetRegistry>(_targetRegistry).register(TargetType.Tool)
                will(returnValue(_targetRegistrationToken))

                oneOf<Disposable>(_targetRegistrationToken).dispose()
            }
        })

        var context = WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(exitCode));
        val actualCommandLines = composer.compose(context).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedCommandLines)
        Assert.assertEquals(context.status, WorkflowStatus.Failed)
    }

    private fun createInstance(parameters: Map<String, String>): WorkflowComposer {
        return VisualStudioWorkflowComposer(
                ParametersServiceStub(parameters),
                ArgumentsServiceStub(),
                _pathService,
                _loggerService,
                _targetService,
                _toolResolver,
                _targetRegistry)
    }
}