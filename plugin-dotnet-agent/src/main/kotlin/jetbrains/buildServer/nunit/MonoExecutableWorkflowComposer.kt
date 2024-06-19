package jetbrains.buildServer.nunit

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.dotnet.ToolState
import jetbrains.buildServer.dotnet.ToolStateWorkflowComposer
import jetbrains.buildServer.mono.MonoToolProvider
import jetbrains.buildServer.rx.observer
import jetbrains.buildServer.util.OSType

class MonoExecutableWorkflowComposer(
    private val _buildStepContext: BuildStepContext,
    private val _virtualContext: VirtualContext,
    private val _cannotExecute: CannotExecute,
    private val _monoToolProvider: MonoToolProvider,
    private val _toolStateWorkflowComposer: ToolStateWorkflowComposer
) : SimpleWorkflowComposer {
    // NotApplicable because it's explicitly called only in NUnitWorkflowComposer,
    // and we don't want it to automatically fold with other composers inside LayeredWorkflowComposer
    override val target = TargetType.NotApplicable

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) = sequence<CommandLine> {
        if (context.status != WorkflowStatus.Running) {
            return@sequence
        }

        for (commandLine in workflow.commandLines) {
            if (_virtualContext.targetOSType == OSType.WINDOWS) {
                yield(commandLine)
                continue
            }

            val executableFile = commandLine.executableFile.path
            val cmdArgs = when {
                executableFile.isNotBlank() ->
                    listOf(CommandLineArgument(executableFile, CommandLineArgumentType.Target)) + commandLine.arguments

                else -> commandLine.arguments
            }

            val monoExecutable = runCatching {
                _monoToolProvider.getPath(
                    MonoConstants.RUNNER_TYPE,
                    _buildStepContext.runnerContext.build,
                    _buildStepContext.runnerContext
                )
            }.getOrNull()?.let(::Path)
            if (monoExecutable == null) {
                _cannotExecute.writeBuildProblemFor(commandLine.executableFile)
                break
            }

            var monoVirtualExecutable: Path? = null
            if (_virtualContext.isVirtual) {
                val toolState = ToolState(
                    ToolPath(monoExecutable),
                    observer { monoVirtualExecutable = it }
                )
                yieldAll(_toolStateWorkflowComposer.compose(context, toolState).commandLines)
            }

            yield(
                CommandLine(
                    commandLine,
                    commandLine.target,
                    monoVirtualExecutable ?: monoExecutable,
                    commandLine.workingDirectory,
                    cmdArgs,
                    commandLine.environmentVariables,
                    commandLine.title,
                    commandLine.description
                )
            )
        }
    }.let(::Workflow)
}
