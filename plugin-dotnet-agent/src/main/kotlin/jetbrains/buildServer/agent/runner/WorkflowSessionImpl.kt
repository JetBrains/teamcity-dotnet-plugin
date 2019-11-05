/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.subjectOf

class WorkflowSessionImpl(
        private val _workflowComposer: WorkflowComposer,
        private val _commandExecutionAdapter: CommandExecutionAdapter)
    : MultiCommandBuildSession, WorkflowContext {

    private val _commandLinesIterator = lazy { _workflowComposer.compose(this).commandLines.iterator() }
    private val _eventSubject = subjectOf<CommandResultEvent>()
    private var _buildFinishedStatus: BuildFinishedStatus? = null

    override fun subscribe(observer: Observer<CommandResultEvent>) = _eventSubject.subscribe(observer)

    override fun getNextCommand(): CommandExecution? {
        if (status != WorkflowStatus.Running) {
            @Suppress("ControlFlowWithEmptyBody")
            // It is required to run code after yields
            while (_commandLinesIterator.value.hasNext()) {}
            return null
        }

        // yield command here
        if (!_commandLinesIterator.value.hasNext()) {
            if (_buildFinishedStatus == null) {
                _buildFinishedStatus = BuildFinishedStatus.FINISHED_SUCCESS
            }

            return null
        }

        _commandExecutionAdapter.initialize(_commandLinesIterator.value.next(), _eventSubject)
        return _commandExecutionAdapter
    }

    override val status: WorkflowStatus
        get() =
            when (_buildFinishedStatus) {
                null -> WorkflowStatus.Running
                BuildFinishedStatus.FINISHED_SUCCESS, BuildFinishedStatus.FINISHED_WITH_PROBLEMS -> WorkflowStatus.Completed
                else -> WorkflowStatus.Failed
            }

    override fun abort(buildFinishedStatus: BuildFinishedStatus) {
        _buildFinishedStatus = buildFinishedStatus
    }

    override fun sessionStarted() = Unit

    override fun sessionFinished(): BuildFinishedStatus? {
        _eventSubject.onComplete()
        return _buildFinishedStatus ?: BuildFinishedStatus.FINISHED_SUCCESS
    }
}