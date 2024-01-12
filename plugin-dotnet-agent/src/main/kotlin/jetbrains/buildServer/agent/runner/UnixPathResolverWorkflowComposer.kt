

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.util.OSType

class UnixPathResolverWorkflowComposer(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _virtualContext: VirtualContext,
        private val _baseResolverWorkflowComposer: PathResolverWorkflowComposer)
    : PathResolverWorkflowComposer {

    override val target: TargetType
        get() = TargetType.SystemDiagnostics

    override fun compose(context: WorkflowContext, state: PathResolverState, workflow: Workflow) =
                if (context.status == WorkflowStatus.Running && _virtualContext.targetOSType != OSType.WINDOWS) {
                    _baseResolverWorkflowComposer.compose(context, PathResolverState(state.pathToResolve, state.virtualPathObserver, commandToResolve))
                }
                else {
                    workflow
                }

    companion object {
        internal val commandToResolve = Path("which")
    }
}