

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Version

interface EnvironmentProvider {
    fun getEnvironmentVariables(toolVersion: Version, toolPlatform: InspectionToolPlatform): Sequence<CommandLineEnvironmentVariable>
}