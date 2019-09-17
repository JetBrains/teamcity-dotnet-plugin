package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
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

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun shouldCompose() {
        // Given
        val composer = createInstance()
        val msbuildVars = listOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var2", "val2"))
        val dotnetVars = listOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var3", "val3"))
        val dotnetArgs = listOf(CommandLineArgument("arg1"), CommandLineArgument("arg2"))
        val msbuildArgs = listOf(CommandLineArgument("arg3"))
        val workingDirectory = File("wd")
        val msbuildExecutable = File("msbuild.exe")
        val dotnetExecutable = File("dotnet.exe")
        val tokens = mutableListOf<Disposable>()

        fun createToken(): Disposable {
            val token = mockk<Disposable>() {
                every { dispose() } returns Unit
            }

            tokens.add(token)
            return token
        }

        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY) } returns Verbosity.Detailed.toString()
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns workingDirectory

        val msbuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns msbuildExecutable
                every { paltform } returns ToolPlatform.Windows
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns msbuildArgs.asSequence()
                every { commandType } returns DotnetCommandType.MSBuild
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0,  emptySet()) } returns emptySet()
                }
            }
        }

        every { _loggerService.writeBlock("msbuild")  } returns createToken()
        every { _environmentVariables.getVariables(Version.Empty) } returns msbuildVars.asSequence()

        val dotnetBuildCommand = mockk<DotnetCommand>() {
            every { toolResolver } returns mockk<ToolResolver>() {
                every { executableFile } returns dotnetExecutable
                every { paltform } returns ToolPlatform.DotnetCore
                every { environmentBuilders } returns sequenceOf(
                        mockk<EnvironmentBuilder>() {
                            every { build(any())  } returns createToken()
                        })
                every { getArguments(any()) } returns dotnetArgs.asSequence()
                every { commandType } returns DotnetCommandType.Build
                every { resultsAnalyzer } returns mockk<ResultsAnalyzer>() {
                    every { analyze(0, emptySet()) } returns emptySet()
                }
            }
        }

        every { _environmentVariables.getVariables(Version(3, 0, 0)) } returns dotnetVars.asSequence()

        every { _loggerService.writeBlock("build")  } returns createToken()

        every { _commandSet.commands } returns sequenceOf(msbuildCommand, dotnetBuildCommand)

        every { _failedTestSource.subscribe(any()) } answers {
            createToken()
        } andThen {
            createToken()
        }

        every { _workflowContext.subscribe(any()) } answers {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        } andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultOutput("dotnet version"))
            createToken()
        } andThen {
            arg<Observer<CommandResultEvent>>(0).onNext(CommandResultExitCode(0))
            createToken()
        }

        every { _versionParser.parse(listOf("dotnet version")) } returns Version(3, 0, 0)

        every { _dotnetWorkflowAnalyzer.registerResult(any(), emptySet(), 0) } returns Unit
        every { _dotnetWorkflowAnalyzer.summarize(any()) } returns Unit
        every { _targetRegistry.register(TargetType.Tool) } returns createToken()
        every { _commandRegistry.register(any()) } returns Unit
        every { _loggerService.writeBlock("Getting .NET Core SDK version")  } returns createToken()

        // When
        val actualCommandLines = composer.compose(_workflowContext).commandLines.toList()

        // Then
        for (token in tokens) {
            verify { token.dispose() }
        }

        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                TargetType.Tool,
                                msbuildExecutable,
                                workingDirectory,
                                msbuildArgs,
                                msbuildVars),
                        CommandLine(
                                TargetType.SystemDiagnostics,
                                dotnetExecutable,
                                workingDirectory,
                                listOf(CommandLineArgument("--version")),
                                msbuildVars),
                        CommandLine(
                                TargetType.Tool,
                                dotnetExecutable,
                                workingDirectory,
                                dotnetArgs,
                                dotnetVars)
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
}