package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ToolPath

data class DotnetBuildContext
(
        val workingDirectory: ToolPath,
        val command: DotnetCommand,
        val toolVersion: Version = Version.Empty,
        val verbosityLevel: Verbosity? = null)