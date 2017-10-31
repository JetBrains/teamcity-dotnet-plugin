package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject

class DiscoveredTarget(private val _name: String, parameters: Map<String, String>) : DiscoveredObject(DotnetConstants.RUNNER_TYPE, parameters.toMutableMap()) {
    override fun toString(): String {
        return _name
    }
}