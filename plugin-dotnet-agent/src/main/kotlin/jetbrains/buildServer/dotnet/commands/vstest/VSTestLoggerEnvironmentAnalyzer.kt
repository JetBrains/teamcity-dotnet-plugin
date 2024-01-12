

package jetbrains.buildServer.dotnet.commands.vstest

import java.io.File

interface VSTestLoggerEnvironmentAnalyzer {
    fun analyze(targets: List<File>)
}