package jetbrains.buildServer.agent.runner

class BuildOptionsImpl(
        private val _buildStepContext: BuildStepContext)
    : BuildOptions {

    override val failBuildOnExitCode: Boolean
        get() = _buildStepContext.runnerContext.build.failBuildOnExitCode
}