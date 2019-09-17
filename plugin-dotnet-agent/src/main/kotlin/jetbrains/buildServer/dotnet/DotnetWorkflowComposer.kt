package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import org.apache.log4j.Logger
import java.io.Closeable
import java.io.File
import java.util.*

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer,
        private val _commandSet: CommandSet,
        private val _failedTestSource: FailedTestSource,
        private val _targetRegistry: TargetRegistry,
        private val _commandRegistry: CommandRegistry,
        private val _versionParser: VersionParser,
        private val _parametersService: ParametersService)
    : WorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow =
            Workflow(sequence {
                val verbosity = _parametersService
                        .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                        ?.trim()
                        ?.let { Verbosity.tryParse(it) }

                val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)
                var dotnetVersions = mutableListOf<Version>()
                val analyzerContext = DotnetWorkflowAnalyzerContext()
                try {
                    for (command in _commandSet.commands) {
                        val executableFile = command.toolResolver.executableFile
                        if (command.toolResolver.paltform == ToolPlatform.DotnetCore && dotnetVersions.isEmpty()) {
                            // Getting .NET Core version
                            yieldAll(getDotnetSdkVersionCommands(context, executableFile, workingDirectory, dotnetVersions))
                        }

                        val dotnetBuildContext = DotnetBuildContext(workingDirectory, command, dotnetVersions.lastOrNull() ?: Version.Empty, verbosity)
                        yieldAll(getDotnetCommands(context, dotnetBuildContext, analyzerContext, executableFile))
                    }
                }
                finally {
                    _dotnetWorkflowAnalyzer.summarize(analyzerContext)
                }
            })

    private fun getDotnetSdkVersionCommands(workflowContext: WorkflowContext, executableFile: File, workingDirectory: File, versions: MutableCollection<Version>): Sequence<CommandLine> =  sequence {
        disposableOf (
                _loggerService.writeBlock("Getting .NET Core SDK version"),
                workflowContext.subscibeForOutput { versions.add(_versionParser.parse(listOf(it))) }
        ).use {
            yield(
                    CommandLine(
                            TargetType.SystemDiagnostics,
                            executableFile,
                            workingDirectory,
                            versionArgs,
                            _defaultEnvironmentVariables.getVariables(Version.Empty).toList()))
        }
    }

    private fun getDotnetCommands(workflowContext: WorkflowContext, dotnetBuildContext: DotnetBuildContext, analyzerContext: DotnetWorkflowAnalyzerContext, executableFile: File): Sequence<CommandLine> = sequence {
        val args = dotnetBuildContext.command.getArguments(dotnetBuildContext).toList()
        val result = EnumSet.noneOf(CommandResult::class.java)

        disposableOf(
                // Build an environment
                disposableOf(dotnetBuildContext.command.environmentBuilders.map { it.build(dotnetBuildContext) }),
                // Strart a build log block
                _loggerService.writeBlock(generateBlockName(dotnetBuildContext.command, args)),
                // Subscribe for failed tests
                _failedTestSource.subscribe { result += CommandResult.FailedTests },
                // Subscribe for an exit code
                workflowContext.subscibeForExitCode {
                    val commandResult = dotnetBuildContext.command.resultsAnalyzer.analyze(it, result)
                    _dotnetWorkflowAnalyzer.registerResult(analyzerContext, commandResult, it)
                    if (commandResult.contains(CommandResult.Fail)) {
                        workflowContext.abort(BuildFinishedStatus.FINISHED_FAILED)
                    }
                },
                // Register the current target
                _targetRegistry.register(target)
        ).use {
            _commandRegistry.register(dotnetBuildContext)
            yield(CommandLine(
                    TargetType.Tool,
                    executableFile,
                    dotnetBuildContext.workingDirectory,
                    args,
                    _defaultEnvironmentVariables.getVariables(dotnetBuildContext.toolVersion).toList()))
        }
    }

    private fun generateBlockName(command: DotnetCommand, args: List<CommandLineArgument>): String {
        val commandName = command.commandType.id.replace('-', ' ')
        return if (commandName.isNotBlank()) {
            commandName
        } else {
            args.firstOrNull()?.value ?: ""
        }
    }

    companion object {
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        private val LOG = Logger.getLogger(DotnetWorkflowComposer::class.java)
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}