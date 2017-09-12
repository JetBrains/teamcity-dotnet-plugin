package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLineResult

interface WorkflowContext {
    val lastResult: CommandLineResult

    fun abort(buildFinishedStatus: BuildFinishedStatus)
}