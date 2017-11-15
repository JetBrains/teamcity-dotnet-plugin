package jetbrains.buildServer.agent.runner

interface WorkflowOutputFilter {
    fun acceptStandardOutput(text: String): Boolean

    fun acceptErrorOutput(text: String): Boolean
}