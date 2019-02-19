package jetbrains.buildServer.dotnet

import java.io.File

data class DotnetBuildContext(
        val workingDirectory: File,
        val command: DotnetCommand,
        val currentSdk: DotnetSdk,
        val verbosityLevel: Verbosity? = null,
        val sdks: Set<DotnetSdk> = emptySet())