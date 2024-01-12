

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Version

interface EnvironmentVariables {
    fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable>
}