package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.emptyDisposable
import org.hamcrest.CustomMatcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Invocation
import org.jmock.lib.action.CustomAction
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.Closeable
import java.io.File
import java.util.*

class DotnetWorkflowComposerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _workflowContext: WorkflowContext
    private lateinit var _environmentBuilder1: EnvironmentBuilder
    private lateinit var _environmentVariables: EnvironmentVariables
    private lateinit var _commandSet: CommandSet
    private lateinit var _dotnetCommand1: DotnetCommand
    private lateinit var _dotnetCommand2: DotnetCommand
    private lateinit var _toolResolver1: ToolResolver
    private lateinit var _toolResolver2: ToolResolver
    private lateinit var _closeable1: Closeable
    private lateinit var _loggerService: LoggerService
    private lateinit var _closeable3: Closeable
    private lateinit var _closeable4: Closeable
    private lateinit var _failedTestSource: FailedTestSource
    private lateinit var _resultsAnalyzer1: ResultsAnalyzer
    private lateinit var _resultsAnalyzer2: ResultsAnalyzer
    private lateinit var _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer
    private lateinit var _targetRegistry: TargetRegistry
    private lateinit var _targetRegistrationToken: Disposable
    private lateinit var _commandRegistry: CommandRegistry
    private lateinit var _buildContextFactory: DotnetBuildContextFactory
    private lateinit var _buildContext1: DotnetBuildContext
    private lateinit var _buildContext2: DotnetBuildContext

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock(PathsService::class.java)
        _failedTestSource = _ctx.mock(FailedTestSource::class.java)
        _dotnetWorkflowAnalyzer = _ctx.mock(DotnetWorkflowAnalyzer::class.java)
        _loggerService = _ctx.mock(LoggerService::class.java)
        _workflowContext = _ctx.mock(WorkflowContext::class.java)
        _environmentVariables = _ctx.mock(EnvironmentVariables::class.java)
        _commandSet = _ctx.mock(CommandSet::class.java)
        _dotnetCommand1 = _ctx.mock(DotnetCommand::class.java, "command1")
        _environmentBuilder1 = _ctx.mock(EnvironmentBuilder::class.java)
        _resultsAnalyzer1 = _ctx.mock(ResultsAnalyzer::class.java, "resultsAnalyzer1")
        _toolResolver1 = _ctx.mock(ToolResolver::class.java, "resolver1")
        _closeable1 = _ctx.mock(Closeable::class.java, "closeable1")
        _dotnetCommand2 = _ctx.mock(DotnetCommand::class.java, "command2")
        _toolResolver2 = _ctx.mock(ToolResolver::class.java, "resolver2")
        _closeable3 = _ctx.mock(Closeable::class.java, "closeable3")
        _closeable4 = _ctx.mock(Closeable::class.java, "closeable4")
        _resultsAnalyzer2 = _ctx.mock(ResultsAnalyzer::class.java, "resultsAnalyzer2")
        _targetRegistry = _ctx.mock(TargetRegistry::class.java)
        _targetRegistrationToken = _ctx.mock(Disposable::class.java)
        _commandRegistry = _ctx.mock(CommandRegistry::class.java)
        _buildContextFactory = _ctx.mock(DotnetBuildContextFactory::class.java)
        _buildContext1 = DotnetBuildContext(File("wd"), _dotnetCommand1, DotnetSdk(File("dotnet"), Version(1)))
        _buildContext2 = DotnetBuildContext(File("wd"), _dotnetCommand2, DotnetSdk(File("dotnet"), Version(2)))
    }

    @Test
    fun shouldCompose() {
        // Given
        val composer = createInstance()
        val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var1", "val1"))
        val args1 = listOf(CommandLineArgument("arg1"), CommandLineArgument("arg2"))
        val args2 = listOf(CommandLineArgument("arg3"))
        val result = CommandLineResult(sequenceOf(0), emptySequence(), emptySequence())

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<DotnetBuildContextFactory>(_buildContextFactory).create(_dotnetCommand1)
                will(returnValue(_buildContext1))

                oneOf<EnvironmentVariables>(_environmentVariables).getVariables(Version(1))
                will(returnValue(envVars.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand1).commandType
                will(returnValue(DotnetCommandType.Build))

                oneOf<DotnetCommand>(_dotnetCommand1).getArguments(_buildContext1)
                will(returnValue(args1.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand1).environmentBuilders
                will(returnValue(sequenceOf(_environmentBuilder1)))

                _ctx.invocation {
                    on(_failedTestSource)
                    count(2)
                    func(_failedTestSource::subscribe)
                    will(object : CustomAction("") {
                        @Suppress("UNCHECKED_CAST")
                        override fun invoke(p0: Invocation?): Any {
                            val observer = p0!!.getParameter(0) as Observer<Unit>
                            observer.onNext(Unit)
                            return emptyDisposable()
                        }
                    })
                    with(object : CustomMatcher<Observer<ServiceMessage>>("") {
                        override fun matches(item: Any?): Boolean = true
                    })
                }

                oneOf<DotnetCommand>(_dotnetCommand1).resultsAnalyzer
                will(returnValue(_resultsAnalyzer1))

                oneOf<ResultsAnalyzer>(_resultsAnalyzer1).analyze(0, EnumSet.of(CommandResult.FailedTests))
                will(returnValue(EnumSet.of(CommandResult.Success)))

                oneOf<DotnetCommand>(_dotnetCommand1).toolResolver
                will(returnValue(_toolResolver1))

                oneOf<ToolResolver>(_toolResolver1).executableFile
                will(returnValue(File("dotnet.exe")))

                oneOf<DotnetBuildContextFactory>(_buildContextFactory).create(_dotnetCommand2)
                will(returnValue(_buildContext2))

                oneOf<EnvironmentVariables>(_environmentVariables).getVariables(Version(2))
                will(returnValue(envVars.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand2).commandType
                will(returnValue(DotnetCommandType.NuGetPush))

                oneOf<DotnetCommand>(_dotnetCommand2).getArguments(_buildContext2)
                will(returnValue(args2.asSequence()))

                oneOf<DotnetCommand>(_dotnetCommand2).environmentBuilders
                will(returnValue(emptySequence<EnvironmentBuilder>()))

                oneOf<DotnetCommand>(_dotnetCommand2).resultsAnalyzer
                will(returnValue(_resultsAnalyzer2))

                oneOf<ResultsAnalyzer>(_resultsAnalyzer2).analyze(0, EnumSet.of(CommandResult.FailedTests))
                will(returnValue(EnumSet.of(CommandResult.Success)))

                oneOf<DotnetCommand>(_dotnetCommand2).toolResolver
                will(returnValue(_toolResolver2))

                oneOf<ToolResolver>(_toolResolver2).executableFile
                will(returnValue(File("msbuild.exe")))

                oneOf<CommandSet>(_commandSet).commands
                will(returnValue(sequenceOf(_dotnetCommand1, _dotnetCommand2)))

                allowing<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(result))

                oneOf<LoggerService>(_loggerService).writeStandardOutput(Pair(".NET Core SDK v1.0.0 ", Color.Default), Pair("dotnet.exe arg1 arg2", Color.Header))

                oneOf<LoggerService>(_loggerService).writeBlock(DotnetCommandType.Build.id)
                will(returnValue(_closeable3))

                oneOf<Closeable>(_closeable3).close()

                oneOf<LoggerService>(_loggerService).writeStandardOutput(Pair(".NET Core SDK v2.0.0 ", Color.Default), Pair("msbuild.exe arg3", Color.Header))

                oneOf<LoggerService>(_loggerService).writeBlock(DotnetCommandType.NuGetPush.id.replace('-', ' '))
                will(returnValue(_closeable4))

                oneOf<Closeable>(_closeable4).close()

                oneOf<EnvironmentBuilder>(_environmentBuilder1).build(_buildContext1)
                will(returnValue(_closeable1))

                oneOf<Closeable>(_closeable1).close()

                allowing<WorkflowContext>(_workflowContext).lastResult
                will(returnValue(result))

                allowing<DotnetWorkflowAnalyzer>(_dotnetWorkflowAnalyzer).registerResult(with(any(DotnetWorkflowAnalyzerContext::class.java))
                        ?: DotnetWorkflowAnalyzerContext(), with(EnumSet.of(CommandResult.Success)) ?: EnumSet.noneOf(CommandResult::class.java), with(0))
                oneOf<DotnetWorkflowAnalyzer>(_dotnetWorkflowAnalyzer).summarize(with(any(DotnetWorkflowAnalyzerContext::class.java)) ?: DotnetWorkflowAnalyzerContext())

                exactly(2).of(_targetRegistry).activate(TargetType.Tool)
                will(returnValue(_targetRegistrationToken))

                exactly(2).of(_targetRegistrationToken).dispose()

                oneOf(_commandRegistry).register(_buildContext1)

                oneOf(_commandRegistry).register(_buildContext2)
            }
        })

        val actualCommandLines = composer.compose(_workflowContext).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                actualCommandLines,
                listOf(
                        CommandLine(
                                TargetType.Tool,
                                File("dotnet.exe"),
                                File("wd"),
                                args1,
                                envVars),
                        CommandLine(
                                TargetType.Tool,
                                File("msbuild.exe"),
                                File("wd"),
                                args2,
                                envVars)
                ))
    }

    private fun createInstance(): DotnetWorkflowComposer {
        return DotnetWorkflowComposer(
                _pathService,
                _loggerService,
                ArgumentsServiceStub(),
                _environmentVariables,
                _dotnetWorkflowAnalyzer,
                _commandSet,
                _failedTestSource,
                _targetRegistry,
                _commandRegistry,
                _buildContextFactory)
    }
}