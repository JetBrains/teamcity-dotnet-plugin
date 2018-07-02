package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import java.io.Closeable
import java.util.*
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer,
        private val _commandSet: CommandSet,
        private val _failedTestSource: FailedTestSource,
        private val _targetRegistry: TargetRegistry,
        private val _commandRegistry: CommandRegistry) : WorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow =
            Workflow(buildSequence {
                val analyzerContext = DotnetWorkflowAnalyzerContext()
                for (command in _commandSet.commands) {
                    val result = EnumSet.noneOf(CommandResult::class.java)
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
                        val commandType = command.commandType
                        val commandName = commandType.id.replace('-', ' ')
                        val blockName = if (commandName.isNotBlank()) {
                            commandName
                        } else {
                            args.firstOrNull()?.value ?: ""
                        }

                        _loggerService.onBlock(blockName).use {
                            _failedTestSource
                                    .subscribe { result += CommandResult.FailedTests }
                                    .use {
                                        _targetRegistry.activate(target).use {
                                            _commandRegistry.register(commandType)
                                            yield(CommandLine(
                                                    TargetType.Tool,
                                                    executableFile,
                                                    _pathsService.getPath(PathType.WorkingDirectory),
                                                    args,
                                                    _defaultEnvironmentVariables.variables.toList()))
                                        }
                                    }
                        }
                    } finally {
                        // Clean the environment
                        for (environmentToken in environmentTokens) {
                            try {
                                environmentToken.close()
                            } catch (ex: Exception) {
                                LOG.error("Error during cleaning environment.", ex)
                            }
                        }
                    }

                    val exitCode = context.lastResult.exitCode
                    val commandResult = command.resultsAnalyzer.analyze(exitCode, result)
                    _dotnetWorkflowAnalyzer.registerResult(analyzerContext, commandResult, exitCode)
                    if (commandResult.contains(CommandResult.Fail)) {
                        context.abort(BuildFinishedStatus.FINISHED_FAILED)
                    }
                }

                _dotnetWorkflowAnalyzer.summarize(analyzerContext)
            })

    companion object {
        private val LOG = Logger.getInstance(DotnetWorkflowComposer::class.java.name)
    }
}