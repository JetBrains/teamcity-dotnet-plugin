package jetbrains.buildServer.agent.runner

class BuildInfoImpl(private val _buildStepContext: BuildStepContext) : BuildInfo {
    override val id: String
        get() = if(_buildStepContext.isAvailable) _buildStepContext.runnerContext.id else ""

    override val name: String
        get() = if(_buildStepContext.isAvailable) _buildStepContext.runnerContext.name else ""

    override val runType: String
        get() = if(_buildStepContext.isAvailable) _buildStepContext.runnerContext.runType else ""
}