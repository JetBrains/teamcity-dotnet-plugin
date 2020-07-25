package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version
import java.io.File

data class DotnetFramework(val platform: Platform, var version: Version, var path: File) {
    override fun toString() = "\".NET Framework ${version} ${platform.id} at \\\"${path}\\\"\""
}