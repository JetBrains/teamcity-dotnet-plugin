package jetbrains.buildServer.agent.runner

class BuildOptionsImpl(
    private val _buildStepContext: BuildStepContext)
    : BuildOptions {

    public override val failBuildOnExitCode: Boolean
        get() = _buildStepContext.runnerContext.build.getFailBuildOnExitCode()
}