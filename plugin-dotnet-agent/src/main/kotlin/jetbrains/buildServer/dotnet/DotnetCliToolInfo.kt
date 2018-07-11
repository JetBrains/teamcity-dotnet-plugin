package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetCliToolInfo {
    fun getVersion(path: File): Version
}