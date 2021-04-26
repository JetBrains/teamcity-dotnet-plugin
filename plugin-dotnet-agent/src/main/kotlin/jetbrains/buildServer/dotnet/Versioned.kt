package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version

interface Versioned {
    val version: Version
}