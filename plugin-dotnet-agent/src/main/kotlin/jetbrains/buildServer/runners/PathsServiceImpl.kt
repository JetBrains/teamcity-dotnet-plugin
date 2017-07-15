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
            PathType.WorkingDirectory -> return _buildStepContext.runnerContext.getWorkingDirectory()
            PathType.Checkout -> return _buildStepContext.runnerContext.getBuild().getCheckoutDirectory()
            PathType.AgentTemp -> return _buildAgentConfigurablePaths.getAgentTempDirectory()
            PathType.BuildTemp -> return _buildAgentConfigurablePaths.getBuildTempDirectory()
            PathType.GlobalTemp -> return _buildAgentConfigurablePaths.getCacheDirectory()
            PathType.Plugins -> return _buildAgentConfiguration.getAgentPluginsDirectory()
            PathType.Tools -> return _buildAgentConfiguration.getAgentToolsDirectory()
            PathType.Lib -> return _buildAgentConfiguration.getAgentLibDirectory()
            PathType.Work -> return _buildAgentConfiguration.getWorkDirectory()
            PathType.System -> return _buildAgentConfiguration.getSystemDirectory()
            PathType.Bin -> return java.io.File(_buildAgentConfiguration.getAgentHomeDirectory(), "bin")
            PathType.Config -> return _buildAgentConfigurablePaths.getAgentConfDirectory()
            PathType.Log -> return _buildAgentConfigurablePaths.getAgentLogsDirectory()
            else -> throw UnsupportedOperationException("Unknown parameterType: $pathType")
        }
    }

    override fun getToolPath(toolName: String): java.io.File = java.io.File(_buildStepContext.runnerContext.getToolPath(toolName))
}