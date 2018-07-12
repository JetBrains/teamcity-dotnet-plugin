package jetbrains.buildServer.dotnet

data class DotnetBuildContext(
        val command: DotnetCommand,
        val verbosityLevel: Verbosity? = null,
        val sdks: Set<DotnetSdk> = emptySet())