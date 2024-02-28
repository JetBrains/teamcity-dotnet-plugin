

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.subjectOf

class WorkflowSessionImpl(
    private val _workflowComposer: BuildToolWorkflowComposer,
    private val _commandExecutionFactory: CommandExecutionFactory
) : MultiCommandBuildSession, WorkflowContext {
    private val _commandLinesIterator = lazy { _workflowComposer.compose(this, Unit).commandLines.iterator() }
    private val _eventSubject = subjectOf<CommandResultEvent>()
    private var _buildFinishedStatus: BuildFinishedStatus? = null
    private var _currentTargetType: TargetType? = null

    override fun subscribe(observer: Observer<CommandResultEvent>) = _eventSubject.subscribe(observer)

    override fun getNextCommand(): CommandExecution? {
        val iterator = _commandLinesIterator.value

        // yield command here
        if (!iterator.hasNext()) {
            if (_buildFinishedStatus == null) {
                _buildFinishedStatus = BuildFinishedStatus.FINISHED_SUCCESS
            }

            _eventSubject.onComplete()
            _currentTargetType = null
            return null
        }

        val commandLine = iterator.next()
        _currentTargetType = commandLine.target
        return _commandExecutionFactory.create(commandLine, _eventSubject)
    }

    override val status get() = when (_buildFinishedStatus) {
        null -> when {
            _currentTargetType == TargetType.PostProcessing -> WorkflowStatus.PostProcessing
            else -> WorkflowStatus.Running
        }
        BuildFinishedStatus.FINISHED_SUCCESS, BuildFinishedStatus.FINISHED_WITH_PROBLEMS -> WorkflowStatus.Completed
        else -> WorkflowStatus.Failed
    }

    override fun abort(buildFinishedStatus: BuildFinishedStatus) {
        _buildFinishedStatus = buildFinishedStatus
    }

    override fun sessionStarted() = Unit

    override fun sessionFinished(): BuildFinishedStatus {
        _eventSubject.onComplete()
        return _buildFinishedStatus ?: BuildFinishedStatus.FINISHED_SUCCESS
    }
}