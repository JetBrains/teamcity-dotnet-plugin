package jetbrains.buildServer.runners

import jetbrains.buildServer.agent.BuildFinishedStatus

interface WorkflowContext {
    val lastResult: CommandLineResult

    fun abort(buildFinishedStatus: BuildFinishedStatus)
}