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
import jetbrains.buildServer.rx.emptySubject
import jetbrains.buildServer.rx.subjectOf

class WorkflowSessionImpl(
        private val _workflowComposer: WorkflowComposer,
        private val _commandExecutionFactory: CommandExecutionFactory)
    : MultiCommandBuildSession, WorkflowContext {

    private val _commandLinesIterator = lazy { _workflowComposer.compose(this).commandLines.iterator() }
    private val _eventSubject = subjectOf<CommandResultEvent>()
    private var _buildFinishedStatus: BuildFinishedStatus? = null

    override fun subscribe(observer: Observer<CommandResultEvent>) = _eventSubject.subscribe(observer)

    override fun getNextCommand(): CommandExecution? {
        val iterator = _commandLinesIterator.value

        // yield command here
        if (!iterator.hasNext()) {
            if (_buildFinishedStatus == null) {
                _buildFinishedStatus = BuildFinishedStatus.FINISHED_SUCCESS
            }

            _eventSubject.onComplete()
            return null
        }

        return _commandExecutionFactory.create(iterator.next(), _eventSubject)
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