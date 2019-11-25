package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Path

interface DotnetVersionProvider {
    fun getVersion(dotnetExecutable: Path, workingDirectory: Path): Version
}