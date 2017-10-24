package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject

class DiscoveredTarget(parameters: Map<String, String>) : DiscoveredObject(DotnetConstants.RUNNER_TYPE, parameters.toMutableMap()) {
}