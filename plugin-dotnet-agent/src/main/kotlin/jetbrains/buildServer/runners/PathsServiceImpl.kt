package jetbrains.buildServer.runners

import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.impl.config.BuildAgentConfigurablePaths
import jetbrains.buildServer.runners.PathType
import jetbrains.buildServer.runners.PathsService
import java.io.File

class PathsServiceImpl(
        private final val _buildStepContext: BuildStepContext,
        private final val _buildAgentConfiguration: BuildAgentConfiguration,
        private final val _buildAgentConfigurablePaths: BuildAgentConfigurablePaths) : PathsService {

    override fun getPath(pathType: PathType): java.io.File {
        when(pathType) {
            PathType.WorkingDirectory -> return _buildStepContext.runnerContext.workingDirectory
            PathType.Checkout -> return _buildStepContext.runnerContext.getBuild().checkoutDirectory
            PathType.AgentTemp -> return _buildAgentConfigurablePaths.agentTempDirectory
            PathType.BuildTemp -> return _buildAgentConfigurablePaths.buildTempDirectory
            PathType.GlobalTemp -> return _buildAgentConfigurablePaths.cacheDirectory
            PathType.Plugins -> return _buildAgentConfiguration.agentPluginsDirectory
            PathType.Tools -> return _buildAgentConfiguration.agentToolsDirectory
            PathType.Lib -> return _buildAgentConfiguration.agentLibDirectory
            PathType.Work -> return _buildAgentConfiguration.workDirectory
            PathType.System -> return _buildAgentConfiguration.systemDirectory
            PathType.Bin -> return java.io.File(_buildAgentConfiguration.agentHomeDirectory, "bin")
            PathType.Config -> return _buildAgentConfigurablePaths.agentConfDirectory
            PathType.Log -> return _buildAgentConfigurablePaths.agentLogsDirectory
            else -> throw UnsupportedOperationException("Unknown parameterType: $pathType")
        }
    }

    override fun getToolPath(toolName: String): File = File(_buildStepContext.runnerContext.getToolPath(toolName))
}