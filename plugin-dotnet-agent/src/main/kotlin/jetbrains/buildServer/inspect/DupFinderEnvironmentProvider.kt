

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Version

class DupFinderEnvironmentProvider : EnvironmentProvider {
    override fun getEnvironmentVariables(toolVersion: Version, toolPlatform: InspectionToolPlatform) = emptySequence<CommandLineEnvironmentVariable>()
}