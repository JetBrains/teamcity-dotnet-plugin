

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.Version

data class DotnetCommandContext(
    val workingDirectory: ToolPath,
    val command: DotnetCommand,
    val toolVersion: Version = Version.Empty,
    val verbosityLevel: Verbosity? = null,
)