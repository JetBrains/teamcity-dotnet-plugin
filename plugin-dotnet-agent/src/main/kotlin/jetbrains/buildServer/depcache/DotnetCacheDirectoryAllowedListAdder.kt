package jetbrains.buildServer.depcache

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildAgent
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.DotnetRunnerCacheDirectoryProvider
import jetbrains.buildServer.util.EventDispatcher

class DotnetCacheDirectoryAllowedListAdder(
    private val _agentLifecycleEventDispatcher: EventDispatcher<AgentLifeCycleListener>,
    private val _dotnetRunnerCacheDirectoryProvider: DotnetRunnerCacheDirectoryProvider
) : AgentLifeCycleAdapter() {

    fun register() {
        _agentLifecycleEventDispatcher.addListener(this)
    }

    public override fun afterAgentConfigurationLoaded(agent: BuildAgent) {
        // we need to add .NET cache directory to the allowed list in order to be able to restore the agent-wide NuGet packages directory
        super.beforeAgentConfigurationLoaded(agent)
        val agentConfiguration = agent.configuration
        var allowedList = agentConfiguration.configurationParameters.get(ARTIFACTS_ALLOWED_LIST_PARAMETER_KEY)
        val dotnetCacheDirectory = _dotnetRunnerCacheDirectoryProvider.getDotnetRunnerCacheDirectory(agentConfiguration).path

        LOG.info(String.format("Adding .NET cache directory %s into the allowedList %s", dotnetCacheDirectory, allowedList))

        allowedList = if (allowedList.isNullOrBlank()) {
            dotnetCacheDirectory
        } else "${allowedList.trim()}$ARTIFACTS_ALLOWED_LIST_SEPARATOR$dotnetCacheDirectory"

        agentConfiguration.addConfigurationParameter(ARTIFACTS_ALLOWED_LIST_PARAMETER_KEY, allowedList!!)
    }

    fun unregister() {
        _agentLifecycleEventDispatcher.removeListener(this)
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetCacheDirectoryAllowedListAdder::class.java)
        private val ARTIFACTS_ALLOWED_LIST_PARAMETER_KEY = "teamcity.artifactDependenciesResolution.allowedList"
        private val ARTIFACTS_ALLOWED_LIST_SEPARATOR = ","
    }
}