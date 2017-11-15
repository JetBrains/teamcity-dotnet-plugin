package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLineResult
import java.io.Closeable

interface WorkflowContext {
    val lastResult: CommandLineResult

    val status: WorkflowStatus

    fun abort(buildFinishedStatus: BuildFinishedStatus)

    fun registerOutputFilter(listener: WorkflowOutputFilter): Closeable
}