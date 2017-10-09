package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.impl.config.BuildAgentConfigurablePaths
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor
import java.io.File
import java.util.*

class PathsServiceImpl(
        private val _buildStepContext: BuildStepContext,
        private val _buildAgentConfiguration: BuildAgentConfiguration,
        private val _buildAgentConfigurablePaths: BuildAgentConfigurablePaths,
        private val _pluginDescriptor: PluginDescriptor) : PathsService {

    override val uniqueName: String
        get() = UUID.randomUUID().toString().replace("-", "")

    override fun getPath(pathType: PathType) = when (pathType) {
        PathType.WorkingDirectory -> _buildStepContext.runnerContext.workingDirectory
        PathType.Checkout -> _buildStepContext.runnerContext.build.checkoutDirectory
        PathType.AgentTemp -> _buildAgentConfigurablePaths.agentTempDirectory
        PathType.BuildTemp -> _buildAgentConfigurablePaths.buildTempDirectory
        PathType.GlobalTemp -> _buildAgentConfigurablePaths.cacheDirectory
        PathType.Plugins -> _buildAgentConfiguration.agentPluginsDirectory
        PathType.Plugin -> _pluginDescriptor.pluginRoot
        PathType.Tools -> _buildAgentConfiguration.agentToolsDirectory
        PathType.Lib -> _buildAgentConfiguration.agentLibDirectory
        PathType.Work -> _buildAgentConfiguration.workDirectory
        PathType.System -> _buildAgentConfiguration.systemDirectory
        PathType.Bin -> File(_buildAgentConfiguration.agentHomeDirectory, "bin")
        PathType.Config -> _buildAgentConfigurablePaths.agentConfDirectory
        PathType.Log -> _buildAgentConfigurablePaths.agentLogsDirectory
    }

    override fun getToolPath(toolName: String): File = File(_buildStepContext.runnerContext.getToolPath(toolName))
}