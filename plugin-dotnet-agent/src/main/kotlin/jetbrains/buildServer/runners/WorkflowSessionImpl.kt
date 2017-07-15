/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.runners

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.*
import java.io.File

class WorkflowSessionImpl(
        _workflowComposer: WorkflowComposer,
        private val _buildStepContext: BuildStepContext,
        private val _loggerService: LoggerService)
    : MultiCommandBuildSession, WorkflowContext {

    private val _commandLinesIterator = _workflowComposer.compose(this).commandLines.iterator()
    private var _lastResult: CommandLineResult? = null;

    override fun sessionStarted() = Unit

    override fun getNextCommand(): CommandExecution? {
        if(!_commandLinesIterator.hasNext()) {
            return null;
        }

        val exitCode = ArrayList<Int>()
        val standardOutput = ArrayList<String>()
        val errorOutput = ArrayList<String>()
        _lastResult = CommandLineResult(exitCode.asSequence(), standardOutput.asSequence(), errorOutput.asSequence())

        return CommandExecutionAdapter(
                _commandLinesIterator.next(),
                exitCode,
                standardOutput,
                errorOutput,
                _buildStepContext,
                _loggerService);
    }

    override fun sessionFinished(): BuildFinishedStatus? = BuildFinishedStatus.FINISHED_SUCCESS

    override val lastResult: CommandLineResult
        get() = _lastResult ?: throw RunBuildException("There are no any results yet");

    private class CommandExecutionAdapter(
            private val _commandLine: CommandLine,
            private val _exitCode: MutableCollection<Int>,
            private val _standardOutput: MutableCollection<String>,
            private val _errorOutput: MutableCollection<String>,
            private val _buildStepContext: BuildStepContext,
            private val _loggerService: LoggerService): CommandExecution {

        override fun beforeProcessStarted() = Unit

        override fun processStarted(programCommandLine: String, workingDirectory: File) = Unit

        override fun processFinished(exitCode: Int) {
            _exitCode.add(exitCode)
        }

        override fun makeProgramCommandLine(): ProgramCommandLine = ProgramCommandLineAdapter(_commandLine, _buildStepContext.runnerContext.getBuildParameters().getEnvironmentVariables())

        override fun onStandardOutput(text: String) {
            _standardOutput.add(text);
            _loggerService.onStandardOutput(text)
        }

        override fun onErrorOutput(text: String) {
            _errorOutput.add(text)
            _loggerService.onErrorOutput(text)
        }

        override fun interruptRequested(): TerminationAction = TerminationAction.NONE

        override fun isCommandLineLoggingEnabled(): Boolean = true
    }

    private class ProgramCommandLineAdapter(
            private val _commandLine: CommandLine,
            private val _environmentVariables: Map<String, String>): ProgramCommandLine {
        override fun getExecutablePath(): String = _commandLine.executableFile.absolutePath

        override fun getWorkingDirectory(): String = _commandLine.workingDirectory.absolutePath

        override fun getArguments(): MutableList<String> = _commandLine.arguments.map { it.value }.toMutableList()

        override fun getEnvironment(): MutableMap<String, String> {
            var environmentVariables = _environmentVariables.toMutableMap()
            _commandLine.environmentVariables.forEach {  environmentVariables[it.name] = it.value }
            return environmentVariables
        }
    }
}