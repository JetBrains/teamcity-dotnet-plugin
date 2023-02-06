/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.sh.ShWorkflowComposer

class CannotExecuteImpl(
        private val _virtualContext: VirtualContext,
        private val _loggerService: LoggerService)
    : CannotExecute {
    override fun writeBuildProblemFor(executablePath: Path) =
        _loggerService.writeBuildProblem(
                CannotExecuteProblemId,
                "Cannot execute",
                if (_virtualContext.isVirtual)
                    "Cannot execute \"$executablePath\". Try to use a different Docker image for this build."
                else
                    "Cannot execute \"$executablePath\". Try to adjust agent requirements and run the build on a different agent."
        )

    companion object {
        internal const val CannotExecuteProblemId = "Cannot execute"
    }
}