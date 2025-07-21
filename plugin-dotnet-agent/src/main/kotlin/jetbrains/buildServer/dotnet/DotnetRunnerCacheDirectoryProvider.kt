package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.BuildAgentConfiguration
import java.io.File

class DotnetRunnerCacheDirectoryProvider {

    fun getDotnetRunnerCacheDirectory(agentConfiguration: BuildAgentConfiguration): File {
        return agentConfiguration.getCacheDirectory(DOTNET_CACHE_DIR)
    }

    companion object {
        val DOTNET_CACHE_DIR = "jetbrains.dotnet.runner"
    }
}