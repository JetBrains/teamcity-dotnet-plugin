package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.Observer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetWorkflowComposerTest {
    @MockK private lateinit var _workflowContext: WorkflowContext
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _commandRegistry: CommandRegistry
    @MockK private lateinit var _targetRegistry: TargetRegistry
    @MockK private lateinit var _failedTestSource: FailedTestSource
    @MockK private lateinit var _commandSet: CommandSet
    @MockK private lateinit var _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer
    @MockK private lateinit var _environmentVariables: EnvironmentVariables
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _commandLinePresentationService: CommandLinePresentationService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _crossPlatformWorkflowFactory: WorkflowFactory<CrossPlatformWorkflowState>

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
            TargetType.SystemDiagnostics,
            _dotnetExecutable.path,
            Path(_workingDirectory.canonicalPath),
            listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
            _msbuildVars,
            "dotnet --version",
            listOf(StdOutText("Getting the .NET SDK version", Color.Header)))

    @BeforeMethod
    fun setUp() {
        _tokens.clear()
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Detailed.toString()
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns _workingDirectory
        every { _environmentVariables.getVariables(Version(3, 0, 0)) } returns _dotnetVars.asSequence()
        every { _dotnetWorkflowAnalyzer.summarize(any()) } returns Unit
        every { _targetRegistry.register(TargetType.Tool) } answers { createToken() }
        every { _commandRegistry.register(any()) } returns Unit
        every { _dotnetWorkflowAnalyzer.registerResult(any(), emptySet(), 0) } returns Unit
        every { _environmentVariables.getVariables(Version.Empty) } returns _msbuildVars.asSequence()
        every { _commandLinePresentationService.buildExecutablePresentation(any()) } answers { listOf(StdOutText(arg<Path>(0).path, Color.Header)) }
        every { _commandLinePresentationService.buildArgsPresentation(any()) } answers { arg<List<CommandLineArgument>>(0).map { StdOutText(" " + it.value) } }
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.resolvePath(File("wd").canonicalPath) } returns _virtualizedWorkingDirectory.path
        every { _workflowContext.status } returns WorkflowStatus.Running
        every { _crossPlatformWorkflowFactory.create(any(), any()) } answers {
            arg<CrossPlatformWorkflowState>(1).versionObserver.onNext(Version(3))
            Workflow(_versionCmd)
        }
    }

    @Test
    fun shouldCompose() {
        // Given
        val composer = createInstance()

        val msbuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executable } returns ToolPath(_msbuildExecutable.path)
                every { paltform } returns ToolPlatform.Windows
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } answers { createToken() }
                        })
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0,  emptySet()) } returns emptySet()
                }
                every { isCommandRequired } returns false
            }
        }

        val dotnetBuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executable } returns _dotnetExecutable
                every { paltform } returns ToolPlatform.CrossPlatform
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } answers { createToken() }
                        })
                every { getArguments(any()) } returns _dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
                every { isCommandRequired } returns true
            }
        }

        every { _commandSet.commands } returns sequenceOf(msbuildCommand, dotnetBuildCommand)

        every { _failedTestSource.subscribe(any()) } /* msbuild */ answers {
            createToken()
        } /* dotnet build */ andThen {
            createToken()
        }

        every { _workflowContext.subscribe(any()) } /* msbuild */ answers {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        } /* dotnet --version */ andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultOutput("3.0.0"))
            createToken()
        } /* dotnet build */ andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        }

        // When
        val actualCommandLines = composer.compose(_workflowContext).commandLines.toList()

        // Then
        verifyAllTokensWereDisposed()

        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                TargetType.Tool,
                                _msbuildExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _msbuildArgs,
                                _msbuildVars,
                                "msbuild arg3",
                                listOf(StdOutText("Windows ", Color.Minor), StdOutText("msbuild.exe", Color.Header), StdOutText(" arg3"))),
                        _versionCmd,
                        CommandLine(
                                TargetType.Tool,
                                _dotnetExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _dotnetArgs,
                                _dotnetVars,
                                "dotnet build",
                                listOf(StdOutText(".NET SDK ", Color.Minor), StdOutText("3.0.0 ", Color.Header), StdOutText("dotnet.exe", Color.Header), StdOutText(" arg1"), StdOutText(" arg2")))
                ))
    }

    @Test
    fun shouldComposeForFailedTest() {
        // Given
        val composer = createInstance()

        every { _dotnetWorkflowAnalyzer.registerResult(any(), setOf(CommandResult.FailedTests), 1) } returns Unit

        val msbuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executable } returns _msbuildExecutable
                every { paltform } returns ToolPlatform.Windows
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } answers { createToken() }
                        })
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(1,  setOf(CommandResult.FailedTests)) } returns setOf(CommandResult.FailedTests)
                }
                every { isCommandRequired } returns false
            }
        }

        val dotnetBuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executable } returns _dotnetExecutable
                every { paltform } returns ToolPlatform.CrossPlatform
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } answers { createToken() }
                        })
                every { getArguments(any()) } returns _dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
                every { isCommandRequired } returns true
            }
        }

        every { _commandSet.commands } returns sequenceOf(msbuildCommand, dotnetBuildCommand)

        every { _failedTestSource.subscribe(any()) } /* msbuild */ answers {
            arg<Observer<Unit>>(0).onNext(Unit)
            createToken()
        } /* dotnet build */ andThen {
            createToken()
        }

        every { _workflowContext.subscribe(any()) } /* msbuild */ answers {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(1))
            createToken()
        } /* dotnet --version */ andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultOutput("3.0.0"))
            createToken()
        } /* dotnet build */ andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        }

        // When
        val actualCommandLines = composer.compose(_workflowContext).commandLines.toList()

        // Then
        verifyAllTokensWereDisposed()

        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                TargetType.Tool,
                                _msbuildExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _msbuildArgs,
                                _msbuildVars,
                                "msbuild arg3",
                                listOf(StdOutText("Windows ", Color.Minor), StdOutText("msbuild.exe", Color.Header), StdOutText(" arg3"))),
                        CommandLine(
                                TargetType.SystemDiagnostics,
                                _dotnetExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
                                _msbuildVars,
                                "dotnet --version",
                                listOf(StdOutText("Getting the .NET SDK version", Color.Header))),
                        CommandLine(
                                TargetType.Tool,
                                _dotnetExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _dotnetArgs,
                                _dotnetVars,
                                "dotnet build",
                                listOf(StdOutText(".NET SDK ", Color.Minor), StdOutText("3.0.0 ", Color.Header), StdOutText("dotnet.exe", Color.Header), StdOutText(" arg1"), StdOutText(" arg2")))
                ))
    }

    @Test
    fun shouldAbortBuildWhenFailed() {
        // Given
        val composer = createInstance()

        every { _dotnetWorkflowAnalyzer.registerResult(any(), setOf(CommandResult.Fail), 1) } returns Unit

        val msbuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executable } returns _msbuildExecutable
                every { paltform } returns ToolPlatform.Windows
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } answers { createToken() }
                        })
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(1,  emptySet()) } returns setOf(CommandResult.Fail)
                }
                every { isCommandRequired } returns false
            }
        }

        val dotnetBuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executable } returns _dotnetExecutable
                every { paltform } returns ToolPlatform.CrossPlatform
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } answers { createToken() }
                        })
                every { getArguments(any()) } returns _dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
                every { isCommandRequired } returns true
            }
        }

        every { _commandSet.commands } returns sequenceOf(msbuildCommand, dotnetBuildCommand)

        every { _failedTestSource.subscribe(any()) } /* msbuild */ answers {
            createToken()
        } /* dotnet build */ andThen {
            createToken()
        }

        every { _workflowContext.subscribe(any()) } /* msbuild */ answers {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(1))
            createToken()
        } /* dotnet --version */ andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultOutput("3.0.0"))
            createToken()
        } /* dotnet build */ andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        }

        every { _workflowContext.abort(BuildFinishedStatus.FINISHED_FAILED) } returns Unit

        // When
        val actualCommandLines = composer.compose(_workflowContext).commandLines.toList()

        // Then
        verifyAllTokensWereDisposed()
        verify { _workflowContext.abort(BuildFinishedStatus.FINISHED_FAILED) }

        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                TargetType.Tool,
                                _msbuildExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _msbuildArgs,
                                _msbuildVars,
                                "msbuild arg3",
                                listOf(StdOutText("Windows ", Color.Minor), StdOutText("msbuild.exe", Color.Header), StdOutText(" arg3"))),
                        CommandLine(
                                TargetType.SystemDiagnostics,
                                _dotnetExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
                                _msbuildVars,
                                "dotnet --version",
                                listOf(StdOutText("Getting the .NET SDK version", Color.Header))),
                        CommandLine(
                                TargetType.Tool,
                                _dotnetExecutable.path,
                                Path(_workingDirectory.canonicalPath),
                                _dotnetArgs,
                                _dotnetVars,
                                "dotnet build",
                                listOf(StdOutText(".NET SDK ", Color.Minor), StdOutText("3.0.0 ", Color.Header), StdOutText("dotnet.exe", Color.Header), StdOutText(" arg1"), StdOutText(" arg2")))
                ))
    }

    private fun createInstance(): DotnetWorkflowComposer {
        return DotnetWorkflowComposer(
                _pathsService,
                _environmentVariables,
                _dotnetWorkflowAnalyzer,
                _commandSet,
                _failedTestSource,
                _targetRegistry,
                _commandRegistry,
                _parametersService,
                _commandLinePresentationService,
                _virtualContext,
                _crossPlatformWorkflowFactory)
    }

    private fun createToken(): Disposable {
        val token = mockk<Disposable>() {
            every { dispose() } returns Unit
        }

        _tokens.add(token)
        return token
    }

    private fun verifyAllTokensWereDisposed() {
        for (token in _tokens) {
            verify { token.dispose() }
        }

        _tokens.clear()
    }

}