package jetbrains.buildServer.script

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*

class CSharpScriptWorkflowComposer(
        private val _buildInfo: BuildInfo,
        private val _commandLineFactory: CommandLineFactory)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) =
            when (_buildInfo.runType) {
                ScriptConstants.RUNNER_TYPE -> Workflow(sequenceOf(_commandLineFactory.create()))
                else -> Workflow()
            }
}