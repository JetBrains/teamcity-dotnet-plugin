package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus

class WorkflowSessionEventDispatcher(
    initialListeners: List<WorkflowSessionEventListener>
) {
    private val _listeners: MutableList<WorkflowSessionEventListener> = initialListeners.toMutableList()

    fun addListener(listener: WorkflowSessionEventListener) {
        _listeners.add(listener)
    }

    fun removeListener(listener: WorkflowSessionEventListener) {
        _listeners.remove(listener)
    }

    fun notifySessionStarted() {
        _listeners.forEach { it.onSessionStarted() }
    }

    fun notifySessionFinished(status: BuildFinishedStatus) {
        _listeners.forEach { it.onSessionFinished(status) }
    }
}