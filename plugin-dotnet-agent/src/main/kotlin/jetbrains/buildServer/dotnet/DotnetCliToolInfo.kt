package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetCliToolInfo {
    fun getVersion(dotnetExecutable: File, path: File): Version
}