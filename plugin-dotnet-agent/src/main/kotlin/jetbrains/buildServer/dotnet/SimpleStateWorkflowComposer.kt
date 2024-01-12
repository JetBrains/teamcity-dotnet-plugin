

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.observer

class SimpleStateWorkflowComposer(
        private val _virtualContext: VirtualContext,
        private val _pathResolverWorkflowComposers: List<PathResolverWorkflowComposer>)
    : ToolStateWorkflowComposer {

    override val target: TargetType
        get() = TargetType.SystemDiagnostics

    override fun compose(context: WorkflowContext, state: ToolState, workflow: Workflow): Workflow = Workflow(
            sequence {
                val executable = state.executable
                var virtualPath: Path? = null
                if (_virtualContext.isVirtual && executable.homePaths.isEmpty()) {
                    // Getting executable
                    val pathResolverState = PathResolverState(
                            executable.virtualPath,
                            observer<Path> {
                                if (virtualPath == null && it.path.isNotBlank()) {
                                    virtualPath = it
                                    state.virtualPathObserver.onNext(it)
                                }
                            }
                    )

                    for (pathResolverWorkflowFactory in _pathResolverWorkflowComposers) {
                        yieldAll(pathResolverWorkflowFactory.compose(context, pathResolverState).commandLines)
                    }
                }

                state.versionObserver?.onNext(Version.Empty)
            }
    )
}