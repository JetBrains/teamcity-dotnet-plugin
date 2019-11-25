package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.util.OSType

class UnixPathResolverWorkflowFactory(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _virtualContext: VirtualContext,
        private val _baseResolverWorkflowFactory: PathResolverWorkflowFactory)
    : PathResolverWorkflowFactory {
    override fun create(context: WorkflowContext, state: PathResolverState) =
                if (context.status == WorkflowStatus.Running && _virtualContext.targetOSType != OSType.WINDOWS) {
                    _baseResolverWorkflowFactory.create(context, PathResolverState(state.pathToResolve, state.virtualPathObserver, commandToResolve))
                }
                else {
                    Workflow()
                }

    companion object {
        internal val commandToResolve = Path("which")
    }
}