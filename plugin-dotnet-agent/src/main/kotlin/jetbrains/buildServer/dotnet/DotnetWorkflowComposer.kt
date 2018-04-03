package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import java.io.Closeable
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _failedTestDetector: FailedTestDetector,
        private val _argumentsService: ArgumentsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _commandSet: CommandSet) : WorkflowComposer, WorkflowOutputFilter {

    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        return Workflow(buildSequence {
            context.registerOutputFilter(this@DotnetWorkflowComposer).use {
                val hasFailedTests = mutableListOf<Boolean>()
                for(command in _commandSet.commands) {
                    // Build the environment
                    val environmentTokens = mutableListOf<Closeable>()
                    for (environmentBuilder in command.environmentBuilders) {
                        environmentTokens.add(environmentBuilder.build(command));
                    }

                    try {
                        val executableFile = command.toolResolver.executableFile
                        val args = command.arguments.toList()
                        val commandHeader = _argumentsService.combine(sequenceOf(executableFile.name).plus(args.map { it.value }))
                        _loggerService.onStandardOutput(commandHeader)
                        _loggerService.onBlock(command.commandType.id.replace('-', ' ')).use {
                            yield(CommandLine(
                                    TargetType.Tool,
                                    executableFile,
                                    _pathsService.getPath(PathType.WorkingDirectory),
                                    args,
                                    _defaultEnvironmentVariables.variables.toList()))
                        }
                    }
                    finally {
                        // Clean the environment
                        for (environmentToken in environmentTokens) {
                            try
                            {
                                environmentToken.close();
                            }
                            catch(ex: Exception) {
                                LOG.error("Error during cleaning environment.", ex)
                            }
                        }
                    }

                    val result = context.lastResult
                    if (!command.resultsAnalyzer.isSuccessful(result)) {
                        _loggerService.onBuildProblem(BuildProblemData.createBuildProblem("dotnet_exit_code${result.exitCode}", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code ${result.exitCode}"))
                        context.abort(BuildFinishedStatus.FINISHED_FAILED)
                        return@buildSequence
                    }
                    else {
                        val hasFailedTest = result.standardOutput.any { _failedTestDetector.hasFailedTest(it) }
                        hasFailedTests.add(hasFailedTest)
                        if (hasFailedTest) {
                            _loggerService.onErrorOutput("Process finished with positive exit code ${result.exitCode} (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
                        }
                    }
                }

                if (hasFailedTests.size > 1 && !hasFailedTests.last() && hasFailedTests.any { it }) {
                    _loggerService.onErrorOutput("Process(es) finished with positive exit code (some tests have failed). Reporting step success as all the tests have run. Use \"at least one test failed\" failure condition to fail the build.")
                }
            }
        })
    }

    override fun acceptStandardOutput(text: String): Boolean {
        return _failedTestDetector.hasFailedTest(text)
    }

    override fun acceptErrorOutput(text: String): Boolean {
        return false
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetWorkflowComposer::class.java.name)
    }
}