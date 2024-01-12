

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.rx.Observer

class InspectionToolState(
    val toolStartInfo: ToolStartInfo,
    val versionObserver: Observer<Version>
)