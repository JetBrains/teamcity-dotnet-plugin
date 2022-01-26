/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType

class LayeredWorkflowComposer(
        private val _workflowComposers: List<SimpleWorkflowComposer>)
    : SimpleWorkflowComposer {
    override val target: TargetType = TargetType.NotApplicable

    override fun compose(
            context: WorkflowContext,
            state:Unit,
            workflow: Workflow): Workflow {
        val toolWorkflows = _workflowComposers
                .filter { it.target == TargetType.Tool }
                .asSequence()
                .map { it.compose(context, state) }

        val otherWorkflowComposers = _workflowComposers
                .filter { it.target != TargetType.NotApplicable && it.target != TargetType.Tool }
                .sortedBy { it.target.priority }

        val workflows = toolWorkflows.map { compose(context, state, it, otherWorkflowComposers) }
        val commandLines = workflows.flatMap { it.commandLines }
        return Workflow(commandLines)
    }

    private fun compose(context: WorkflowContext, state:Unit, toolWorkflow: Workflow, otherWorkflowComposers: List<WorkflowComposer<Unit>>): Workflow =
            otherWorkflowComposers.fold(toolWorkflow) { acc, it -> it.compose(context, state, acc) }
}