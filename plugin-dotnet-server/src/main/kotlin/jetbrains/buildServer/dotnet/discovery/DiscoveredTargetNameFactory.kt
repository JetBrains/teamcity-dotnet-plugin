package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.DotnetCommandType

interface DiscoveredTargetNameFactory {
    fun createName(commandType: DotnetCommandType, path: String): String
}