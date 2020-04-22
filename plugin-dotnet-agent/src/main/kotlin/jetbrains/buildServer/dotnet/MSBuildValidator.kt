package jetbrains.buildServer.dotnet

import java.io.File

interface MSBuildValidator {
    fun isValide(msbuildBasePath: File): Boolean
}