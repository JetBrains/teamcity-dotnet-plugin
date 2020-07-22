package jetbrains.buildServer.dotnet

import java.io.File

data class DotnetFramework(val platform: Platform, var version: Version, var path: File) {
    override fun toString() = "\".NET Framework ${version} ${platform.id} at \\\"${path}\\\"\""
}