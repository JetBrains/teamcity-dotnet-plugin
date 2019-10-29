package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.rx.Observer

class CrossPlatformWorkflowState(
        public val executable: ToolPath,
        val virtualPathObserver: Observer<Path>,
        val versionObserver: Observer<Version>)