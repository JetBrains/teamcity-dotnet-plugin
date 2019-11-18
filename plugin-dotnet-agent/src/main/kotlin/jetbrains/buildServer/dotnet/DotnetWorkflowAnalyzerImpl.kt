package jetbrains.buildServer.dotnet

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.runner.LoggerService
import java.util.*

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