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

package jetbrains.buildServer.custom

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.observer
import java.io.File

class CustomCommandWorkflowComposer(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathsService: PathsService,
        private val _targetService: TargetService)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow): Workflow {
        parameters(DotnetConstants.PARAM_COMMAND)?.let {
            if (!DotnetCommandType.Custom.id.equals(it, true)) {
                return Workflow()
            }
        } ?: return Workflow()

        if (context.status != WorkflowStatus.Running) {
            return Workflow()
        }

        val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).path)
        val args = parameters(DotnetConstants.PARAM_ARGUMENTS)?.trim()?.let {
            _argumentsService.split(it).map { CommandLineArgument(it, CommandLineArgumentType.Custom) }.toList()
        } ?: emptyList<CommandLineArgument>()

        val targets = _targetService.targets.map { it.target }.toMutableList()
        if (targets.isEmpty()) {
            // to run dotnet tools
            targets.add(Path(""))
        }

        return Workflow(targets.asSequence().map { CommandLine(null, target, it, workingDirectory, args) })
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}