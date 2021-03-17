package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.Version

interface ToolVersionProvider {
    fun getVersion(parameters: Map<String, String>): Version
}