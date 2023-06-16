package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version

data class DotnetWorkload(
    val name: String,
    val sdkVersion: Version
)