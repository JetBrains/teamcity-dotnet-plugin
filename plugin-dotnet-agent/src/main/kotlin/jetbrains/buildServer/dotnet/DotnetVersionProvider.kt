package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Path
import java.io.File

interface DotnetVersionProvider {
    fun getVersion(dotnetExecutable: Path, workingDirectory: Path): Version
}