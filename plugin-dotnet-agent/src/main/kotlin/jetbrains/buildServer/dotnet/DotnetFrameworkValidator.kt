package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetFrameworkValidator {
    fun isValid(framework: DotnetFramework): Boolean
}