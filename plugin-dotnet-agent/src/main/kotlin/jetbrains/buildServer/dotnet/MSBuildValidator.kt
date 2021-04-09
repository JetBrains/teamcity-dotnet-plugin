package jetbrains.buildServer.dotnet

import java.io.File

interface MSBuildValidator {
    fun isValid(msbuildBasePath: File): Boolean
}