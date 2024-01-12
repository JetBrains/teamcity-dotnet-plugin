

package jetbrains.buildServer.dotnet.discovery.dotnetFramework

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.Platform
import java.io.File

data class DotnetFramework(val platform: Platform, var version: Version, var path: File) {
    override fun toString() = "\".NET Framework ${version} ${platform.id} at \\\"${path}\\\"\""
}