package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.OSType

class UnixPathResolverWorkflowComposer(
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _virtualContext: VirtualContext,
    private val _baseResolverWorkflowComposer: PathResolverWorkflowComposer
) : PathResolverWorkflowComposer {
    override fun compose(context: WorkflowContext, state: PathResolverState, workflow: Workflow) = when {
        context.status == WorkflowStatus.Running && onNonWindowsOS ->
            _baseResolverWorkflowComposer.compose(
                context = context,
                state = PathResolverState(state.pathToResolve, state.virtualPathObserver, commandToResolve)
            )
        else -> workflow
    }

    private val onNonWindowsOS get() = _virtualContext.targetOSType != OSType.WINDOWS

    companion object {
        internal val commandToResolve = Path("which")
    }
}