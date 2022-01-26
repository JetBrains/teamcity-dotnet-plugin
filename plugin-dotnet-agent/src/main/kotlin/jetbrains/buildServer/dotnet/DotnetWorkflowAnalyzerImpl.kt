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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.runner.LoggerService

class DotnetWorkflowAnalyzerImpl(private val _loggerService: LoggerService)
    : DotnetWorkflowAnalyzer {

    override fun registerResult(context: DotnetWorkflowAnalyzerContext, result: Set<CommandResult>, exitCode: Int) {
        if (result.contains(CommandResult.Fail)) {
            _loggerService.writeBuildProblem("dotnet_exit_code$exitCode", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code $exitCode")
        } else {
            context.addResult(result)
            if (result.contains(CommandResult.FailedTests)) {
                _loggerService.writeWarning("Process finished with positive exit code $exitCode (some tests have failed). Reporting step success as all the tests have run.")
            }
        }
    }

    override fun summarize(context: DotnetWorkflowAnalyzerContext) {
        if (!context.results.any()) {
            return
        }

        val lastCommandIsSucceeded = !context.results.last().contains(CommandResult.FailedTests)
        val hasFailedTests = context.results.any { it.contains(CommandResult.FailedTests) }
        if (lastCommandIsSucceeded && hasFailedTests) {
            _loggerService.writeWarning("Some of processes finished with positive exit code (some tests have failed). Reporting step success as all the tests have run.")
        }
    }
}