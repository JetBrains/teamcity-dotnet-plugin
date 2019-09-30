package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Path
import java.io.File

data class DotnetBuildContext(
        val workingDirectory: Path,
        val command: DotnetCommand,
        val toolVersion: Version = Version.Empty,
        val verbosityLevel: Verbosity? = null)