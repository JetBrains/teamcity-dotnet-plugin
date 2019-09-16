package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import org.apache.log4j.Logger
import java.io.Closeable
import java.util.*

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer,
        private val _commandSet: CommandSet,
        private val _failedTestSource: FailedTestSource,
        private val _targetRegistry: TargetRegistry,
        private val _commandRegistry: CommandRegistry,
        private val _contextFactory: DotnetBuildContextFactory,
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
                val analyzerContext = DotnetWorkflowAnalyzerContext()
                var dotnetSdk: DotnetSdk? = null
                for (command in _commandSet.commands) {
                    val executableFile = command.toolResolver.executableFile
                    if (command.toolResolver.paltform == ToolPlatform.DotnetCore) {
                        // Getting .NET Core version
                        if (dotnetSdk == null) {
                            disposableOf (
                                    _loggerService.writeBlock("Getting .NET Core version"),
                                    context.subscribe {
                                        when {
                                            it is CommandResultOutput -> {
                                                _versionParser.tryParse(sequenceOf(it.output))?.let {
                                                    dotnetSdk = DotnetSdk(executableFile, Version.parse(it))
                                                }
                                            }
                                        }
                                }).use {
                                    yield(
                                            CommandLine(
                                                    TargetType.SystemDiagnostics,
                                                    executableFile,
                                                    workingDirectory,
                                                    versionArgs,
                                                    _defaultEnvironmentVariables.getVariables(Version.Empty).toList()))
                                }
                        }

                        if (dotnetSdk == null) {
                            dotnetSdk = DotnetSdk(executableFile, Version.Empty)
                        }
                    }

                    val dotnetBuildContext = DotnetBuildContext(
                            workingDirectory,
                            command,
                            dotnetSdk ?: DotnetSdk(executableFile, Version.Empty),
                            verbosity,
                            emptySet())

                    val args = command.getArguments(dotnetBuildContext).toList()

                    // Define build log block name
                    val commandType = command.commandType
                    val commandName = commandType.id.replace('-', ' ')
                    val blockName = if (commandName.isNotBlank()) {
                        commandName
                    } else {
                        args.firstOrNull()?.value ?: ""
                    }

                    val result = EnumSet.noneOf(CommandResult::class.java)

                    disposableOf(
                            // Build an environment
                            disposableOf(command.environmentBuilders.map { it.build(dotnetBuildContext) }),
                            // Strart a build log block
                            _loggerService.writeBlock(blockName),
                            // Subscribe for failed tests
                            _failedTestSource.subscribe { result += CommandResult.FailedTests },
                            // Subscribe for an exit code
                            context.subscribe {
                                when {
                                    it is CommandResultExitCode -> {
                                        val commandResult = command.resultsAnalyzer.analyze(it.exitCode, result)
                                        _dotnetWorkflowAnalyzer.registerResult(analyzerContext, commandResult, it.exitCode)
                                        if (commandResult.contains(CommandResult.Fail)) {
                                            context.abort(BuildFinishedStatus.FINISHED_FAILED)
                                        }
                                    }
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
                                _defaultEnvironmentVariables.getVariables(dotnetBuildContext.currentSdk.version).toList()))
                    }
                }

                _dotnetWorkflowAnalyzer.summarize(analyzerContext)
            })

    companion object {
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        private val LOG = Logger.getLogger(DotnetWorkflowComposer::class.java)
        private val sdkInfoRegex = "^(.+)\\s*\\[(.+)\\]$".toRegex()
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}