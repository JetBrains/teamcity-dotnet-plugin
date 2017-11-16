package jetbrains.buildServer.dotnet

import java.io.File

interface VSTestLoggerEnvironmentAnalyzer {
    fun analyze(targets: List<File>)
}