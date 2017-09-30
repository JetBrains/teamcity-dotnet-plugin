package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.WorkflowComposer
import jetbrains.buildServer.agent.runner.WorkflowContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.Closeable
import java.io.File

class DotnetWorkflowComposerTest {
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _workflowContext: WorkflowContext? = null
    private var _vstestLoggerEnvironment: VSTestLoggerEnvironment? = null
    private var _environmentVariables: EnvironmentVariables? = null
    private var _commandSet: CommandSet? = null
    private var _dotnetCommand1: DotnetCommand? = null
    private var _dotnetCommand2: DotnetCommand? = null
    private var _toolResolver1: ToolResolver? = null
    private var _toolResolver2: ToolResolver? = null
    private var _closeable1: Closeable? = null
    private var _closeable2: Closeable? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock(PathsService::class.java)
        _workflowContext = _ctx!!.mock(WorkflowContext::class.java)
        _environmentVariables = _ctx!!.mock(EnvironmentVariables::class.java)
        _vstestLoggerEnvironment = _ctx!!.mock(VSTestLoggerEnvironment::class.java)
        _commandSet = _ctx!!.mock(CommandSet::class.java)
        _dotnetCommand1 = _ctx!!.mock(DotnetCommand::class.java, "command1")
        _toolResolver1 = _ctx!!.mock(ToolResolver::class.java, "resolver1")
        _closeable1 = _ctx!!.mock(Closeable::class.java, "closeable1")
        _dotnetCommand2 = _ctx!!.mock(DotnetCommand::class.java, "command2")
        _toolResolver2 = _ctx!!.mock(ToolResolver::class.java, "resolver2")
        _closeable2 = _ctx!!.mock(Closeable::class.java, "closeable2")
    }

    @Test
    fun shouldCompose() {
        // Given
        val workingDirectory = File("workingDir")
        val composer = createInstance()
        val envVars = listOf<CommandLineEnvironmentVariable>(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var1", "val1"))
        val args1 = listOf<CommandLineArgument>(CommandLineArgument("arg1"), CommandLineArgument("arg2"))
        val args2 = listOf<CommandLineArgument>(CommandLineArgument("arg3"))

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                allowing<EnvironmentVariables>(_environmentVariables).variables
                will(returnValue(envVars.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand1).targetArguments
                will(returnValue(sequenceOf(TargetArguments(sequenceOf(CommandLineArgument("my.csproj"))))))

                oneOf<DotnetCommand>(_dotnetCommand1).arguments
                will(returnValue(args1.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand1).isSuccessfulExitCode(0)
                will(returnValue(true))

                oneOf<DotnetCommand>(_dotnetCommand1).toolResolver
                will(returnValue(_toolResolver1))

                oneOf<ToolResolver>(_toolResolver1).executableFile
                will(returnValue(File("dotnet.exe")))

                oneOf<DotnetCommand>(_dotnetCommand2).targetArguments
                will(returnValue(emptySequence<TargetArguments>()))

                oneOf<DotnetCommand>(_dotnetCommand2).arguments
                will(returnValue(args2.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand2).isSuccessfulExitCode(0)
                will(returnValue(true))

                oneOf<DotnetCommand>(_dotnetCommand2).toolResolver
                will(returnValue(_toolResolver2))

                oneOf<ToolResolver>(_toolResolver2).executableFile
                will(returnValue(File("msbuild.exe")))

                oneOf<CommandSet>(_commandSet).commands
                will(returnValue(sequenceOf(_dotnetCommand1, _dotnetCommand2)))

                allowing<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                allowing<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(CommandLineResult(sequenceOf(0), emptySequence(), emptySequence())))

                oneOf<VSTestLoggerEnvironment>(_vstestLoggerEnvironment).configure(listOf(File("my.csproj")))
                will(returnValue(_closeable1))

                oneOf<Closeable>(_closeable1).close()

                oneOf<VSTestLoggerEnvironment>(_vstestLoggerEnvironment).configure(emptyList())
                will(returnValue(_closeable2))

                oneOf<Closeable>(_closeable2).close()

                allowing<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(CommandLineResult(sequenceOf(0), emptySequence(), emptySequence())))
            }
        })

        val actualCommandLines = composer.compose(_workflowContext!!).commandLines.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                TargetType.Tool,
                                File("dotnet.exe"),
                                workingDirectory,
                                args1,
                                envVars),
                        CommandLine(
                                TargetType.Tool,
                                File("msbuild.exe"),
                                workingDirectory,
                                args2,
                                envVars)
                ))
    }

    private fun createInstance(): WorkflowComposer {
        return DotnetWorkflowComposer(
                _pathService!!,
                _environmentVariables!!,
                _vstestLoggerEnvironment!!,
                _commandSet!!)
    }
}