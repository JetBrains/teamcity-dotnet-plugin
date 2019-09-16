package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetVersionProvider {
    fun getVersion(dotnetExecutable: File, path: File): Version
}