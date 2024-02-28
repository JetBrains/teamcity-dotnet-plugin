package jetbrains.buildServer.script

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class CSharpScriptWorkflowComposer(
    private val _buildInfo: BuildInfo,
    private val _commandLineFactory: CommandLineFactory,
    private val _buildOptions: BuildOptions,
    private val _loggerService: LoggerService,
) : BuildToolWorkflowComposer {
    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) =
            when (_buildInfo.runType) {
                ScriptConstants.RUNNER_TYPE -> Workflow(createCommandLines(context))
                else -> Workflow()
            }

    private fun createCommandLines(context: WorkflowContext) = sequence<CommandLine> {
        var exitCode = 0;
        val commandLine = _commandLineFactory.create()
        disposableOf(
                context.filter { it.SourceId == commandLine.Id }.toExitCodes().subscribe { exitCode = it; }
        ).use {
            yield(commandLine)
        }

        if (exitCode != 0 && _buildOptions.failBuildOnExitCode) {
            _loggerService.writeBuildProblem("csi_exit_code$exitCode", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code $exitCode")
        }
    }
}