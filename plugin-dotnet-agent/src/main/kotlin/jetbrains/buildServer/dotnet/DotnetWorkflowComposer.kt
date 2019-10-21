package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.toDisposable
import jetbrains.buildServer.rx.use
import org.apache.log4j.Logger
import java.io.Closeable
import java.io.File

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
        private val _parametersService: ParametersService,
        private val _commandLinePresentationService: CommandLinePresentationService,
        private val _virtualContext: VirtualContext)
    : WorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow =
            Workflow(sequence {
                val verbosity = _parametersService
                        .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                        ?.trim()
                        ?.let { Verbosity.tryParse(it) }

                val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)
                val virtualWorkingDirectory = Path(_virtualContext.resolvePath(workingDirectory.path))
                var dotnetVersions = mutableListOf<Version>()
                val analyzerContext = DotnetWorkflowAnalyzerContext()
                for (command in _commandSet.commands) {
                    val executable = command.toolResolver.executable
                    if (command.toolResolver.paltform == ToolPlatform.CrossPlatform && dotnetVersions.isEmpty()) {
                        // Getting .NET Core version
                        yieldAll(getDotnetSdkVersionCommands(context, executable.virtualPath, workingDirectory, dotnetVersions))
                    }

                    val dotnetBuildContext = DotnetBuildContext(ToolPath(workingDirectory, virtualWorkingDirectory), command, dotnetVersions.lastOrNull() ?: Version.Empty, verbosity)

                    val args = dotnetBuildContext.command.getArguments(dotnetBuildContext).toList()
                    yieldAll(getDotnetCommands(context, dotnetBuildContext, analyzerContext, executable.virtualPath, args))
                }

                _dotnetWorkflowAnalyzer.summarize(analyzerContext)
            })

    private fun getDotnetSdkVersionCommands(workflowContext: WorkflowContext, executableFile: Path, workingDirectory: Path, versions: MutableCollection<Version>): Sequence<CommandLine> =  sequence {
        disposableOf (
                workflowContext.subscibeForOutput { versions.add(_versionParser.parse(listOf(it))) }
        ).use {
            yield(
                    CommandLine(
                            TargetType.SystemDiagnostics,
                            executableFile,
                            workingDirectory,
                            VersionArgs,
                            _defaultEnvironmentVariables.getVariables(Version.Empty).toList(),
                            getTitle(executableFile, false, "", VersionArgs),
                            listOf(StdOutText("Getting .NET SDK version", Color.Header))))
        }
    }

    private fun getDotnetCommands(workflowContext: WorkflowContext, dotnetBuildContext: DotnetBuildContext, analyzerContext: DotnetWorkflowAnalyzerContext, executableFile: Path, args: List<CommandLineArgument>): Sequence<CommandLine> = sequence {
        val result = mutableSetOf<CommandResult>()

        disposableOf(
                // Build an environment
                dotnetBuildContext.command.environmentBuilders.map { it.build(dotnetBuildContext) }.toDisposable(),
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
                    dotnetBuildContext.workingDirectory.path,
                    args,
                    _defaultEnvironmentVariables.getVariables(dotnetBuildContext.toolVersion).toList(),
                    getTitle(executableFile, dotnetBuildContext.command.toolResolver.isCommandRequired, dotnetBuildContext.command.commandType.id, args),
                    getDescription(dotnetBuildContext, executableFile, args)))
        }
    }

    private fun getTitle(executableFile: Path, isCommandRequired: Boolean, command: String, args: List<CommandLineArgument>): String {
        val executable = File(executableFile.path).nameWithoutExtension
        val commandName = command.replace('-', ' ')
        return if (isCommandRequired && commandName.isNotBlank()) {
            "$executable $commandName"
        } else {
            args.firstOrNull()?.let { "$executable ${it.value}" } ?: executable
        }
    }

    private fun getDescription(dotnetBuildContext: DotnetBuildContext, executableFile: Path, args: List<CommandLineArgument>): List<StdOutText> {
        var description = mutableListOf<StdOutText>()
        when (dotnetBuildContext.command.toolResolver.paltform) {
            ToolPlatform.CrossPlatform -> description.add(StdOutText(".NET SDK ", Color.Minor))
            ToolPlatform.Mono-> description.add(StdOutText("Mono ", Color.Minor))
            ToolPlatform.Windows-> description.add(StdOutText("Windows ", Color.Minor))
        }

        if (dotnetBuildContext.toolVersion != Version.Empty) {
            description.add(StdOutText("${dotnetBuildContext.toolVersion} ", Color.Header))
        }

        description.addAll(_commandLinePresentationService.buildExecutablePresentation(executableFile))
        description.addAll(_commandLinePresentationService.buildArgsPresentation(args))
        return description
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetWorkflowComposer::class.java)
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        internal val VersionArgs = listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory))
    }
}