package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetLogger
import jetbrains.buildServer.dotnet.Logger
import java.io.File

class DotnetLoggerStub(val _loggerFile: File?): DotnetLogger {
    override fun tryGetToolPath(logger: Logger): File? = _loggerFile
}