package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.distinct
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.map
import jetbrains.buildServer.rx.use

class BasePathResolverWorkflowFactory(
        private val _pathsService: PathsService,
        private val _virtualContext: VirtualContext)
    : PathResolverWorkflowFactory {
    override fun create(context: WorkflowContext, state: PathResolverState) = Workflow (
            sequence {
                context
                        .toOutput()
                        .distinct()
                        .filter { it.endsWith(state.pathToResolve.path) }
                        .map { Path(it) }
                        .subscribe(state.virtualPathObserver)
                        .use {
                            yield(CommandLine(
                                    null,
                                    TargetType.SystemDiagnostics,
                                    state.commandToResolve,
                                    Path(_pathsService.getPath(PathType.WorkingDirectory).path),
                                    listOf(CommandLineArgument(state.pathToResolve.path, CommandLineArgumentType.Target)),
                                    emptyList(),
                                    "get ${state.pathToResolve.path}"))
                        }
            }
    )
}