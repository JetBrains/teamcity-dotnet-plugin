package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.*
import java.io.Closeable
import kotlin.coroutines.experimental.buildSequence

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _failedTestDetector: FailedTestDetector,
        private val _argumentsService: ArgumentsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer,
        private val _commandSet: CommandSet) : WorkflowComposer, WorkflowOutputFilter {

    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        return Workflow(buildSequence {
            context.registerOutputFilter(this@DotnetWorkflowComposer).use {
                val analyzerContext = DotnetWorkflowAnalyzerContext()
                for(command in _commandSet.commands) {
                    // Build the environment
                    val environmentTokens = mutableListOf<Closeable>()
                    for (environmentBuilder in command.environmentBuilders) {
                        environmentTokens.add(environmentBuilder.build(command))
                    }

                    try {
                        val executableFile = command.toolResolver.executableFile
                        val args = command.arguments.toList()
                        val commandHeader = _argumentsService.combine(sequenceOf(executableFile.name).plus(args.map { it.value }))
                        _loggerService.onStandardOutput(commandHeader)
                        val commandName = command.commandType.id.replace('-', ' ')
                        val blockName = if (commandName.isNotBlank()) {
                            commandName
                        } else {
                            args.firstOrNull()?.value ?: ""
                        }
                        _loggerService.onBlock(blockName).use {
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
                                environmentToken.close()
                            }
                            catch(ex: Exception) {
                                LOG.error("Error during cleaning environment.", ex)
                            }
                        }
                    }

                    val result = context.lastResult
                    val commandResult = command.resultsAnalyzer.analyze(result)
                    _dotnetWorkflowAnalyzer.registerResult(analyzerContext, commandResult, result.exitCode)
                    if (commandResult.contains(CommandResult.Fail)) {
                        context.abort(BuildFinishedStatus.FINISHED_FAILED)
                        return@buildSequence
                    }
                }

                _dotnetWorkflowAnalyzer.summarize(analyzerContext)
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