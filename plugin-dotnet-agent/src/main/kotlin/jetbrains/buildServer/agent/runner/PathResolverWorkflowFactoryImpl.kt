package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType

class PathResolverWorkflowFactoryImpl(
        private val _virtualContext: VirtualContext)
    : PathResolverWorkflowFactory {
    override fun create(context: WorkflowContext, state: PathResolverState) = Workflow (
            sequence {
                disposableOf(
                        context.subscibeForOutput { if (state.resolvedPath == null) state.resolvedPath = Path(it) },
                        context.subscibeForExitCode { if (it != 0) state.resolvedPath = null }
                ).use {
                    val cmd = when(_virtualContext.targetOSType) {
                        OSType.WINDOWS -> "whereis"
                        else -> "which"
                    }

                    yield(CommandLine(
                            TargetType.SystemDiagnostics,
                            Path(cmd),
                            Path(""),
                            listOf(CommandLineArgument(state.pathToResolve.path, CommandLineArgumentType.Target)),
                            emptyList(),
                            "Getting the ${state.pathToResolve} path"))
                }
            }
    )
}