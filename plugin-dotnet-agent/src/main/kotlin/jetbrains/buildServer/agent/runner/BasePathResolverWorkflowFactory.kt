package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.*
import java.lang.Exception

class BasePathResolverWorkflowFactory(
        private val _pathsService: PathsService,
        private val _virtualContext: VirtualContext)
    : PathResolverWorkflowFactory {
    override fun create(context: WorkflowContext, state: PathResolverState) = Workflow (
            sequence {
                val pathSource = observable<Path> {
                        context
                                .distinct()
                                .subscribe(
                                    { event ->
                                        when {
                                            event is CommandResultOutput -> onNext(Path(event.output))
                                            event is CommandResultExitCode -> if (event.exitCode == 0) onComplete() else onError(Exception("Error"))
                                        }
                                    },
                                    { onError(it) },
                                    { onComplete() })
                }

                pathSource
                        .subscribe(state)
                        .use {
                            yield(CommandLine(
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