

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformer
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.Observer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetWorkflowComposerTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _environmentVariables: EnvironmentVariables
    @MockK private lateinit var _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer
    @MockK private lateinit var _failedTestSource: FailedTestSource
    @MockK private lateinit var _commandRegistry: CommandRegistry
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _commandLinePresentationService: CommandLinePresentationService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _dotnetCommandResolver: DotnetCommandResolver
    @MockK private lateinit var _dotnetCommandsTransformer: DotnetCommandsTransformer

    @MockK private lateinit var _workflowContext: WorkflowContext
    @MockK private lateinit var _dotnetToolStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _msbuildToolStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _resultsObserver: Observer<CommandResultEvent>

    private val _msbuildVars = listOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var2", "val2"))
    private val _dotnetVars = listOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var3", "val3"))
    private val _dotnetArgs = listOf(CommandLineArgument("arg1"), CommandLineArgument("arg2"))
    private val _msbuildArgs = listOf(CommandLineArgument("arg3"))
    private val _workingDirectory = File("wd")
    private val _virtualizedWorkingDirectory = Path("wd")
    private val _msbuildExecutable = ToolPath(Path("msbuild.exe"))
    private val _dotnetExecutable = ToolPath(Path("dotnet.exe"))
    private val _tokens = mutableListOf<Disposable>()
    private val _versionCmd = CommandLine(
        null,
        TargetType.SystemDiagnostics,
        _dotnetExecutable.path,
        Path(_workingDirectory.canonicalPath),
        listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
        _msbuildVars,
        "dotnet --version",
        listOf(StdOutText("Getting the .NET SDK version"))
    )

    @BeforeMethod
    fun setUp() {
        _tokens.clear()
        MockKAnnotations.init(this, relaxed = true)
        clearAllMocks()
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Detailed.toString()
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns _workingDirectory
        every { _environmentVariables.getVariables(Version(3, 0, 0)) } returns _dotnetVars.asSequence()
        every { _dotnetWorkflowAnalyzer.summarize(any()) } returns Unit
        every { _commandRegistry.register(any()) } returns Unit
        every { _dotnetWorkflowAnalyzer.registerResult(any(), emptySet(), 0) } returns Unit
        every { _environmentVariables.getVariables(Version.Empty) } returns _msbuildVars.asSequence()
        every { _commandLinePresentationService.buildExecutablePresentation(any()) } answers { listOf(StdOutText(arg<Path>(0).path)) }
        every { _commandLinePresentationService.buildArgsPresentation(any()) } answers { arg<List<CommandLineArgument>>(0).map { StdOutText(" " + it.value) } }
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.resolvePath(File("wd").canonicalPath) } returns _virtualizedWorkingDirectory.path
        every { _workflowContext.status } returns WorkflowStatus.Running
        every { _dotnetToolStateWorkflowComposer.compose(any(), any()) } answers {
            arg<ToolState>(1).versionObserver?.onNext(Version(3))
            Workflow(_versionCmd)
        }
        every { _msbuildToolStateWorkflowComposer.compose(any(), any()) } answers {
            arg<ToolState>(1).versionObserver?.onNext(Version.Empty)
            Workflow()
        }
        every { _resultsObserver.onNext(any()) } returns Unit
    }

    @Test
    fun `should compose`() {
        // arrange
        val composer = createInstance()

        val msbuildCommand = mockk<DotnetCommand>() {
            every { isAuxiliary } returns false
            every { title } returns ""
            every { toolResolver } returns mockk<ToolResolver>() {
                every { toolStateWorkflowComposer } returns _msbuildToolStateWorkflowComposer
                every { executable } returns ToolPath(_msbuildExecutable.path)
                every { platform } returns ToolPlatform.Windows
                every { environmentBuilders } returns listOf(createEnvironmentBuilderMock(), createEnvironmentBuilderMock())
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0,  emptySet()) } returns emptySet()
                }
                every { isCommandRequired } returns false
                every { resultsObserver } returns _resultsObserver
            }
        }

        val dotnetBuildCommand = createDotnetCommand()
        val dotnetBuildCommand2 = createDotnetCommand()

        every { _dotnetCommandResolver.command } returns msbuildCommand
        every { _dotnetCommandsTransformer.apply(any(), any()) } returns sequenceOf(msbuildCommand, dotnetBuildCommand, dotnetBuildCommand2)

        every { _failedTestSource.subscribe(any()) } /* msbuild */ answers {
            createToken()
        } /* dotnet build */ andThenAnswer {
            createToken()
        }

        every { _workflowContext.subscribe(any()) } /* msbuild */ answers {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        } /* dotnet --version */ andThenAnswer  {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultOutput("3.0.0"))
            createToken()
        } /* dotnet build */ andThenAnswer {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        }

        // act
        val actualCommandLines = composer.compose(_workflowContext, Unit).commandLines.toList()

        // assert
        verifyAllTokensWereDisposed()

        Assert.assertEquals(
            actualCommandLines,
            listOf(
                CommandLine(
                    null,
                    TargetType.Tool,
                    _msbuildExecutable.path,
                    Path(_workingDirectory.canonicalPath),
                    _msbuildArgs,
                    _msbuildVars,
                    "msbuild",
                    emptyList()
                ),
                _versionCmd,
                CommandLine(
                    null,
                    TargetType.Tool,
                    _dotnetExecutable.path,
                    Path(_workingDirectory.canonicalPath),
                    _dotnetArgs,
                    _dotnetVars,
                    "dotnet",
                    listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("3.0.0 ", Color.Header))
                ),
                CommandLine(
                    null,
                    TargetType.Tool,
                    _dotnetExecutable.path,
                    Path(_workingDirectory.canonicalPath),
                    _dotnetArgs,
                    _dotnetVars,
                    "dotnet",
                    listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("3.0.0 ", Color.Header))
                )
            ))
    }

    private fun createDotnetCommand(): DotnetCommand {
        return mockk<DotnetCommand>() {
            every { isAuxiliary } returns false
            every { title } returns ""
            every { toolResolver } returns mockk<ToolResolver>() {
                every { toolStateWorkflowComposer } returns _dotnetToolStateWorkflowComposer
                every { executable } returns _dotnetExecutable
                every { platform } returns ToolPlatform.CrossPlatform
                every { environmentBuilders } returns listOf(createEnvironmentBuilderMock(), createEnvironmentBuilderMock())
                every { getArguments(any()) } returns _dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
                every { isCommandRequired } returns true
                every { resultsObserver } returns _resultsObserver
            }
        }
    }

    @Test
    fun `should compose for failed test`() {
        // arrange
        val composer = createInstance()

        every { _dotnetWorkflowAnalyzer.registerResult(any(), setOf(CommandResult.FailedTests), 1) } returns Unit

        val msbuildCommand = mockk<DotnetCommand>() {
            every { isAuxiliary } returns false
            every { title } returns ""
            every { toolResolver } returns mockk<ToolResolver>() {
                every { toolStateWorkflowComposer } returns _msbuildToolStateWorkflowComposer
                every { executable } returns _msbuildExecutable
                every { platform } returns ToolPlatform.Windows
                every { environmentBuilders } returns listOf(createEnvironmentBuilderMock(), createEnvironmentBuilderMock())
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(1,  setOf(CommandResult.FailedTests)) } returns setOf(CommandResult.FailedTests)
                }
                every { isCommandRequired } returns false
                every { resultsObserver } returns _resultsObserver
            }
        }

        val dotnetBuildCommand = createDotnetCommand()

        every { _dotnetCommandResolver.command } returns msbuildCommand
        every { _dotnetCommandsTransformer.apply(any(), any()) } returns sequenceOf(msbuildCommand, dotnetBuildCommand)

        every { _failedTestSource.subscribe(any()) } /* msbuild */ answers {
            arg<Observer<Unit>>(0).onNext(Unit)
            createToken()
        } /* dotnet build */ andThenAnswer {
            createToken()
        }

        every { _workflowContext.subscribe(any()) } /* msbuild */ answers {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(1))
            createToken()
        } /* dotnet --version */ andThenAnswer {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultOutput("3.0.0"))
            createToken()
        } /* dotnet build */ andThenAnswer {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        }

        // act
        val actualCommandLines = composer.compose(_workflowContext, Unit).commandLines.toList()

        // assert
        verifyAllTokensWereDisposed()

        Assert.assertEquals(
            actualCommandLines,
            listOf(
                CommandLine(
                    null,
                    TargetType.Tool,
                    _msbuildExecutable.path,
                    Path(_workingDirectory.canonicalPath),
                    _msbuildArgs,
                    _msbuildVars,
                    "msbuild",
                    emptyList()
                ),
                CommandLine(
                    null,
                    TargetType.SystemDiagnostics,
                    _dotnetExecutable.path,
                    Path(_workingDirectory.canonicalPath),
                    listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
                    _msbuildVars,
                    "dotnet --version",
                    listOf(StdOutText("Getting the .NET SDK version"))
                ),
                CommandLine(
                    null,
                    TargetType.Tool,
                    _dotnetExecutable.path,
                    Path(_workingDirectory.canonicalPath),
                    _dotnetArgs,
                    _dotnetVars,
                    "dotnet",
                    listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("3.0.0 ", Color.Header))
                )
            ))
    }

    @Test
    fun `should abort build when failed`() {
        // arrange
        val composer = createInstance()

        every { _dotnetWorkflowAnalyzer.registerResult(any(), setOf(CommandResult.Fail), 1) } returns Unit

        val msbuildCommand = mockk<DotnetCommand>() {
            every { isAuxiliary } returns false
            every { title } returns ""
            every { toolResolver } returns mockk<ToolResolver>() {
                every { toolStateWorkflowComposer } returns _msbuildToolStateWorkflowComposer
                every { executable } returns _msbuildExecutable
                every { platform } returns ToolPlatform.Windows
                every { environmentBuilders } returns listOf(createEnvironmentBuilderMock(), createEnvironmentBuilderMock())
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(1,  emptySet()) } returns setOf(CommandResult.Fail)
                }
                every { isCommandRequired } returns false
                every { resultsObserver } returns _resultsObserver
            }
        }

        val dotnetBuildCommand = createDotnetCommand()

        every { _dotnetCommandResolver.command } returns msbuildCommand
        every { _dotnetCommandsTransformer.apply(any(), any()) } returns sequenceOf(msbuildCommand, dotnetBuildCommand)

        every { _failedTestSource.subscribe(any()) } /* msbuild */ answers {
            createToken()
        } /* dotnet build */ andThenAnswer {
            createToken()
        }

        // Subscribe command results observer
        every { _workflowContext.subscribe(any()) } /* msbuild */ answers {
            createToken()
        } andThenAnswer
        /* // Subscribe for an exit code for msbuild */ {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(1))
            createToken()
        } /* Subscribe for an exit code for dotnet --version */ andThenAnswer {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultOutput("3.0.0"))
            createToken()
        } /* Subscribe for an exit code for dotnet build */ andThenAnswer {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        }

        every { _workflowContext.abort(BuildFinishedStatus.FINISHED_FAILED) } returns Unit

        // act
        val actualCommandLines = composer.compose(_workflowContext, Unit).commandLines.toList()

        // assert
        verifyAllTokensWereDisposed()
        verify { _workflowContext.abort(BuildFinishedStatus.FINISHED_FAILED) }

        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                null,
                                TargetType.Tool,
                                _msbuildExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _msbuildArgs,
                                _msbuildVars,
                                "msbuild",
                                emptyList()),
                        CommandLine(
                                null,
                                TargetType.SystemDiagnostics,
                                _dotnetExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
                                _msbuildVars,
                                "dotnet --version",
                                listOf(StdOutText("Getting the .NET SDK version"))),
                        CommandLine(
                                null,
                                TargetType.Tool,
                                _dotnetExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _dotnetArgs,
                                _dotnetVars,
                                "dotnet",
                                listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("3.0.0 ", Color.Header)))
                ))
    }

    @Test
    fun `should provide default variables and variables from environment builders in command line`() {
        // Arrange
        val defaultVariable = mockk<CommandLineEnvironmentVariable>()
        val builder1Variable = mockk<CommandLineEnvironmentVariable>()
        val builder2Variable = mockk<CommandLineEnvironmentVariable>()

        _environmentVariables = mockk<EnvironmentVariables> { every { getVariables(any()) } returns sequenceOf(defaultVariable) }
        val environmentBuildResult1 = mockk<EnvironmentBuildResult>(relaxed = true) { every { variables } returns sequenceOf(builder1Variable) }
        val environmentBuildResult2 = mockk<EnvironmentBuildResult>(relaxed = true) { every { variables } returns sequenceOf(builder2Variable) }
        val builder1: EnvironmentBuilder = mockk<EnvironmentBuilder> { every { build(any()) } returns environmentBuildResult1 }
        val builder2: EnvironmentBuilder = mockk<EnvironmentBuilder> { every { build(any()) } returns environmentBuildResult2 }

        val command = mockk<DotnetCommand>(relaxed = true) { every { environmentBuilders } returns listOf(builder1, builder2) }
        every { _dotnetCommandResolver.command } returns command
        every { _dotnetCommandsTransformer.apply(any(), any()) } returns sequenceOf(command)

        val composer = createInstance()

        // Act
        val workflow = composer.compose(_workflowContext, Unit)

        // Assert
        val commandLineVariables = workflow.commandLines.first().environmentVariables
        Assert.assertEquals(commandLineVariables.size, 3)
        Assert.assertEquals(commandLineVariables[0], defaultVariable)
        Assert.assertEquals(commandLineVariables[1], builder1Variable)
        Assert.assertEquals(commandLineVariables[2], builder2Variable)
    }

    @Test
    fun `should process command exit codes after disposing environment build results`() {
        // Arrange
        val exitCodes = listOf(100, 101)

        val environmentBuildResult1 = mockk<EnvironmentBuildResult>(relaxed = true)
        val environmentBuildResult2 = mockk<EnvironmentBuildResult>(relaxed = true)
        val builder1: EnvironmentBuilder = mockk<EnvironmentBuilder> { every { build(any()) } returns environmentBuildResult1 }
        val builder2: EnvironmentBuilder = mockk<EnvironmentBuilder> { every { build(any()) } returns environmentBuildResult2 }

        val resultsAnalyzer = mockk<ResultsAnalyzer>(relaxed = true)

        val command = mockk<DotnetCommand>(relaxed = true) {
            every { environmentBuilders } returns listOf(builder1, builder2)
            every { this@mockk.resultsAnalyzer } returns resultsAnalyzer
        }
        every { _dotnetCommandResolver.command } returns command
        every { _dotnetCommandsTransformer.apply(any(), any()) } returns sequenceOf(command)
        every { _workflowContext.subscribe(any()) } answers {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(exitCodes[0]))
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(exitCodes[1]))
            mockk<Disposable>(relaxed = true)
        }

        val composer = createInstance()

        // Act
        composer.compose(_workflowContext, Unit).commandLines.toList()

        // Assert
        verifyOrder {
            environmentBuildResult1.dispose()
            environmentBuildResult2.dispose()
            resultsAnalyzer.analyze(exitCodes[0], any())
            _dotnetWorkflowAnalyzer.registerResult(any(), any(), exitCodes[0])
            resultsAnalyzer.analyze(exitCodes[1], any())
            _dotnetWorkflowAnalyzer.registerResult(any(), any(), exitCodes[1])
        }
    }

    private fun createInstance() = DotnetWorkflowComposer(
        _pathsService,
        _environmentVariables,
        _dotnetWorkflowAnalyzer,
        _failedTestSource,
        _commandRegistry,
        _parametersService,
        _virtualContext,
        _dotnetCommandResolver,
        _dotnetCommandsTransformer,
        )

    private fun createToken(): Disposable {
        val token = mockk<Disposable>() {
            every { dispose() } returns Unit
        }

        _tokens.add(token)
        return token
    }

    private fun createEnvironmentBuilderMock(): EnvironmentBuilder {
        val environmentBuildResult = mockk<EnvironmentBuildResult>(relaxed = true)
        _tokens.add(environmentBuildResult)

        return mockk<EnvironmentBuilder>(relaxed = true) {
            every { build(any()) } returns environmentBuildResult
        }
    }

    private fun verifyAllTokensWereDisposed() {
        for (token in _tokens) {
            verify { token.dispose() }
        }

        _tokens.clear()
    }
}