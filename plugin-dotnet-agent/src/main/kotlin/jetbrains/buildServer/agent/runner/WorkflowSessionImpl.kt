/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import java.io.File

class WorkflowSessionImpl(
        private val _workflowComposer: WorkflowComposer,
        private val _buildStepContext: BuildStepContext,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService)
    : MultiCommandBuildSession, WorkflowContext {

    private var _commandLinesIterator: Iterator<CommandLine>? = null
    private var _lastResult: CommandLineResult? = null
    private var _buildFinishedStatus: BuildFinishedStatus? = null

    override fun getNextCommand(): CommandExecution? {
        val commandLinesIterator: Iterator<CommandLine> = _commandLinesIterator ?: _workflowComposer.compose(this).commandLines.iterator()
        _commandLinesIterator = commandLinesIterator

        if (status != WorkflowStatus.Running) {
            return null
        }

        // yield command here
        if (!commandLinesIterator.hasNext()) {
            if (_buildFinishedStatus == null) {
               _buildFinishedStatus = BuildFinishedStatus.FINISHED_SUCCESS
            }

            return null
        }

        val exitCode = ArrayList<Int>()
        _lastResult = CommandLineResult(exitCode.asSequence(), emptySequence(), emptySequence())

        return CommandExecutionAdapter(
                commandLinesIterator.next(),
                exitCode,
                _buildStepContext,
                _loggerService,
                _argumentsService)
    }

    override val status: WorkflowStatus
        get() {
            val curStatus = _buildFinishedStatus
            if (curStatus == null) {
                return WorkflowStatus.Running
            }

            when (curStatus) {
                BuildFinishedStatus.FINISHED_SUCCESS, BuildFinishedStatus.FINISHED_WITH_PROBLEMS -> return WorkflowStatus.Completed
                else -> return WorkflowStatus.Failed
            }
        }

    override fun abort(buildFinishedStatus: BuildFinishedStatus) {
        _buildFinishedStatus = buildFinishedStatus
    }

    override fun sessionStarted() = Unit

    override fun sessionFinished(): BuildFinishedStatus? =
        _buildFinishedStatus ?: BuildFinishedStatus.FINISHED_SUCCESS

    override val lastResult: CommandLineResult
        get() = _lastResult ?: throw RunBuildException("There are no any results yet")

    private class CommandExecutionAdapter(
            private val _commandLine: CommandLine,
            private val _exitCode: MutableCollection<Int>,
            private val _buildStepContext: BuildStepContext,
            private val _loggerService: LoggerService,
            private val _argumentsService: ArgumentsService) : CommandExecution {

        override fun beforeProcessStarted() = Unit

        override fun processStarted(programCommandLine: String, workingDirectory: File) = Unit

        override fun processFinished(exitCode: Int) {
            _exitCode.add(exitCode)
        }

        override fun makeProgramCommandLine(): ProgramCommandLine = ProgramCommandLineAdapter(
                _argumentsService,
                _commandLine,
                _buildStepContext.runnerContext.buildParameters.environmentVariables)

        override fun onStandardOutput(text: String)= _loggerService.onStandardOutput(text)

        override fun onErrorOutput(text: String) = _loggerService.onErrorOutput(text)

        override fun interruptRequested(): TerminationAction = TerminationAction.KILL_PROCESS_TREE

        override fun isCommandLineLoggingEnabled(): Boolean = true
    }

    private class ProgramCommandLineAdapter(
            private val _argumentsService: ArgumentsService,
            private val _commandLine: CommandLine,
            private val _environmentVariables: Map<String, String>) : ProgramCommandLine {
        override fun getExecutablePath(): String =
                _commandLine.executableFile.absolutePath

        override fun getWorkingDirectory(): String =
                _commandLine.workingDirectory.absolutePath

        override fun getArguments(): MutableList<String> =
                _commandLine.arguments.map { it.value }.toMutableList()

        override fun getEnvironment(): MutableMap<String, String> {
            val environmentVariables = _environmentVariables.toMutableMap()
            _commandLine.environmentVariables.forEach { environmentVariables[it.name] = it.value }
            return environmentVariables
        }
    }
}