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
    @MockK private lateinit var _versionParser: VersionParser
    @MockK private lateinit var _commandRegistry: CommandRegistry
    @MockK private lateinit var _targetRegistry: TargetRegistry
    @MockK private lateinit var _failedTestSource: FailedTestSource
    @MockK private lateinit var _commandSet: CommandSet
    @MockK private lateinit var _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer
    @MockK private lateinit var _environmentVariables: EnvironmentVariables
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _pathsService: PathsService

    private val _msbuildVars = listOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var2", "val2"))
    private val _dotnetVars = listOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var3", "val3"))
    private val _dotnetArgs = listOf(CommandLineArgument("arg1"), CommandLineArgument("arg2"))
    private val _msbuildArgs = listOf(CommandLineArgument("arg3"))
    private val _workingDirectory = File("wd")
    private val _msbuildExecutable = File("msbuild.exe")
    private val _dotnetExecutable = File("dotnet.exe")
    private val _tokens = mutableListOf<Disposable>()

    @BeforeMethod
    fun setUp() {
        _tokens.clear()
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Detailed.toString()
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns _workingDirectory
        every { _loggerService.writeBlock("Getting .NET Core SDK version")  } returns createToken()
        every { _versionParser.parse(listOf("3.0.0")) } returns Version(3, 0, 0)
        every { _environmentVariables.getVariables(Version(3, 0, 0)) } returns _dotnetVars.asSequence()
        every { _dotnetWorkflowAnalyzer.summarize(any()) } returns Unit
        every { _targetRegistry.register(TargetType.Tool) } returns createToken()
        every { _commandRegistry.register(any()) } returns Unit
        every { _dotnetWorkflowAnalyzer.registerResult(any(), emptySet(), 0) } returns Unit
        every { _loggerService.writeBlock("msbuild")  } returns createToken()
        every { _environmentVariables.getVariables(Version.Empty) } returns _msbuildVars.asSequence()
        every { _loggerService.writeBlock("build")  } returns createToken()
    }

    @Test
    fun shouldCompose() {
        // Given
        val composer = createInstance()

        every { _loggerService.writeStandardOutput(Pair("Windows ", Color.Minor), Pair("msbuild.exe", Color.Header), Pair(" arg3", Color.Default)) } returns Unit
        every { _loggerService.writeStandardOutput(Pair(".NET Core SDK ", Color.Minor), Pair("3.0.0 ", Color.Minor), Pair("dotnet.exe", Color.Header), Pair(" arg1", Color.Default), Pair(" arg2", Color.Default)) } returns Unit

        val msbuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns _msbuildExecutable
                every { paltform } returns ToolPlatform.Windows
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0,  emptySet()) } returns emptySet()
                }
            }
        }

        val dotnetBuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns _dotnetExecutable
                every { paltform } returns ToolPlatform.CrossPlatform
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns _dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
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
                                _msbuildExecutable,
                                _workingDirectory,
                                _msbuildArgs,
                                _msbuildVars),
                        CommandLine(
                                TargetType.SystemDiagnostics,
                                _dotnetExecutable,
                                _workingDirectory,
                                listOf(CommandLineArgument("--version")),
                                _msbuildVars),
                        CommandLine(
                                TargetType.Tool,
                                _dotnetExecutable,
                                _workingDirectory,
                                _dotnetArgs,
                                _dotnetVars)
                ))
    }

    @Test
    fun shouldComposeForFailedTest() {
        // Given
        val composer = createInstance()

        every { _dotnetWorkflowAnalyzer.registerResult(any(), setOf(CommandResult.FailedTests), 1) } returns Unit
        every { _loggerService.writeStandardOutput(Pair("Windows ", Color.Minor), Pair("msbuild.exe", Color.Header), Pair(" arg3", Color.Default)) } returns Unit
        every { _loggerService.writeStandardOutput(Pair(".NET Core SDK ", Color.Minor), Pair("3.0.0 ", Color.Minor), Pair("dotnet.exe", Color.Header), Pair(" arg1", Color.Default), Pair(" arg2", Color.Default)) } returns Unit

        val msbuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns _msbuildExecutable
                every { paltform } returns ToolPlatform.Windows
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(1,  setOf(CommandResult.FailedTests)) } returns setOf(CommandResult.FailedTests)
                }
            }
        }

        val dotnetBuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns _dotnetExecutable
                every { paltform } returns ToolPlatform.CrossPlatform
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns _dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
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
                                _msbuildExecutable,
                                _workingDirectory,
                                _msbuildArgs,
                                _msbuildVars),
                        CommandLine(
                                TargetType.SystemDiagnostics,
                                _dotnetExecutable,
                                _workingDirectory,
                                listOf(CommandLineArgument("--version")),
                                _msbuildVars),
                        CommandLine(
                                TargetType.Tool,
                                _dotnetExecutable,
                                _workingDirectory,
                                _dotnetArgs,
                                _dotnetVars)
                ))
    }

    @Test
    fun shouldAbortBuildWhenFailed() {
        // Given
        val composer = createInstance()

        every { _dotnetWorkflowAnalyzer.registerResult(any(), setOf(CommandResult.Fail), 1) } returns Unit
        every { _loggerService.writeStandardOutput(Pair("Windows ", Color.Minor), Pair("msbuild.exe", Color.Header), Pair(" arg3", Color.Default)) } returns Unit
        every { _loggerService.writeStandardOutput(Pair(".NET Core SDK ", Color.Minor), Pair("3.0.0 ", Color.Minor), Pair("dotnet.exe", Color.Header), Pair(" arg1", Color.Default), Pair(" arg2", Color.Default)) } returns Unit

        val msbuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns _msbuildExecutable
                every { paltform } returns ToolPlatform.Windows
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns _msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(1,  emptySet()) } returns setOf(CommandResult.Fail)
                }
            }
        }

        val dotnetBuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns _dotnetExecutable
                every { paltform } returns ToolPlatform.CrossPlatform
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns _dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
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
                                _msbuildExecutable,
                                _workingDirectory,
                                _msbuildArgs,
                                _msbuildVars),
                        CommandLine(
                                TargetType.SystemDiagnostics,
                                _dotnetExecutable,
                                _workingDirectory,
                                listOf(CommandLineArgument("--version")),
                                _msbuildVars),
                        CommandLine(
                                TargetType.Tool,
                                _dotnetExecutable,
                                _workingDirectory,
                                _dotnetArgs,
                                _dotnetVars)
                ))
    }

    private fun createInstance(): DotnetWorkflowComposer {
        return DotnetWorkflowComposer(
                _pathsService,
                _loggerService,
                _environmentVariables,
                _dotnetWorkflowAnalyzer,
                _commandSet,
                _failedTestSource,
                _targetRegistry,
                _commandRegistry,
                _versionParser,
                _parametersService)
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