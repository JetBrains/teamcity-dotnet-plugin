package jetbrains.buildServer.dotnet.discovery.msbuild

import java.io.File

interface MSBuildValidator {
    fun isValid(msbuildBasePath: File): Boolean
}