package jetbrains.buildServer.agent.runner

enum class WorkflowStatus {
    Running,        // workflow is running main commands
    PostProcessing, // workflow is running post-processing commands
    Completed,      // workflow is finished (successfully or with build problems)
    Failed;         // workflow is interrapted or aboted

    companion object {
        val WorkflowStatus.isStopped get() = this == Failed || this == Completed
    }
}