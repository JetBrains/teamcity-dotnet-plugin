

package jetbrains.buildServer

import jetbrains.buildServer.dotnet.Version

interface ToolVersionProvider {
    fun getVersion(toolPath: String?, toolTypeName: String): Version
}