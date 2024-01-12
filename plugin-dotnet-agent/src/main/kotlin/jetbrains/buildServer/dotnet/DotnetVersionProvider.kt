

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.Version

interface DotnetVersionProvider {
    fun getVersion(dotnetExecutable: Path, workingDirectory: Path): Version
}