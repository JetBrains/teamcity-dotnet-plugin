package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetLogger {
    fun tryGetToolPath(logger: Logger): File?
}