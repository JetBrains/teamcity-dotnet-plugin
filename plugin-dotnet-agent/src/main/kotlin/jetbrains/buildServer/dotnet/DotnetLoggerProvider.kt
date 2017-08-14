package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetLoggerProvider {
    fun tryGetToolPath(logger: Logger): File?
}