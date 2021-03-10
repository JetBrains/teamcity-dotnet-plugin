package jetbrains.buildServer.agent.runner

class BuildInfoImpl(private val _buildStepContext: BuildStepContext) : BuildInfo {
    override val runType: String
        get() = _buildStepContext.runnerContext.runType
}