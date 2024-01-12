

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.rx.Observer

data class ToolState(
    val executable: ToolPath,
    val virtualPathObserver: Observer<Path>,
    val versionObserver: Observer<Version>? = null,
)