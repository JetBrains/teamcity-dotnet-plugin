package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetCliToolInfo {
    fun getInfo(dotnetExecutable: File, path: File): DotnetInfo
}