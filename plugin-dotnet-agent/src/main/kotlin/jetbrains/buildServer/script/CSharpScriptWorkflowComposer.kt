package jetbrains.buildServer.script

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.inspect.InspectionArguments
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType

class CSharpScriptWorkflowComposer(
        private val _buildInfo: BuildInfo,
        private val _commandLineFactory: CommandLineFactory,
        private val _buildOptions: BuildOptions)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) =
            when (_buildInfo.runType) {
                ScriptConstants.RUNNER_TYPE -> Workflow(createCommandLines(context))
                else -> Workflow()
            }

    private fun createCommandLines(context: WorkflowContext) = sequence<CommandLine> {
        var hasErrors = false;
        val commandLine = _commandLineFactory.create()
        disposableOf(
                context.filter { it.SourceId == commandLine.Id }.toExitCodes().subscribe { hasErrors = it != 0; }
        ).use {
            yield(commandLine)
        }

        if (_buildOptions.failBuildOnExitCode && hasErrors) {
            context.abort(BuildFinishedStatus.FINISHED_FAILED)
        }
    }
}