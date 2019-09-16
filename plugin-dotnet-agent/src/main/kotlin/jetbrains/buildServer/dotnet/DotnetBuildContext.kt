package jetbrains.buildServer.dotnet

import java.io.File

data class DotnetBuildContext(
        val workingDirectory: File,
        val command: DotnetCommand,
        val toolVersion: Version = Version.Empty,
        val verbosityLevel: Verbosity? = null)