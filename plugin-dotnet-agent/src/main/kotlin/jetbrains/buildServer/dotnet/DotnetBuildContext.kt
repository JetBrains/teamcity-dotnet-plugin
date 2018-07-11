package jetbrains.buildServer.dotnet

data class DotnetBuildContext(val command: DotnetCommand, val sdks: Set<DotnetSdk> = emptySet())