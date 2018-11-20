package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe

class BuildServerShutdownMonitor(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _parametersService: ParametersService,
        private val _environmentVariables: EnvironmentVariables)
    : CommandRegistry {

    private var _subscriptionToken: Disposable
    private var _contexts = mutableListOf<DotnetBuildContext>()

    init {
        _subscriptionToken = agentLifeCycleEventSources.buildFinishedSource.subscribe {
            if (_contexts.size > 0) {
                try {
                    LOG.debug("Has a build command")
                    val sdks = _contexts
                            .flatMap {
                                val context = it
                                context.sdks.map { Pair(context, it) }
                            }
                            .filter { it.second.version > Version.LastVersionWithoutSharedCompilation }
                            .distinctBy { it.second.path }

                    val executableFile = _dotnetToolResolver.executableFile
                    for (sdkInfo in sdks) {
                        val context = sdkInfo.first
                        val sdk = sdkInfo.second
                        LOG.debug("${sdk.version} is greater then ${Version.LastVersionWithoutSharedCompilation} in the \"${sdk.path}\"")
                        val envVariables = _environmentVariables.getVariables(context).toList()
                        _commandLineExecutor.tryExecute(
                                CommandLine(
                                        TargetType.Tool,
                                        executableFile,
                                        sdk.path,
                                        shutdownArgs,
                                        envVariables)
                        )
                    }
                } finally {
                    _contexts.clear()
                }
            }
        }
    }

    override fun register(context: DotnetBuildContext) {
        if (buildCommands.contains(context.command.commandType)) {
            val useSharedCompilation = _parametersService.tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)?.equals("true", true) ?: true
            if (useSharedCompilation) {
                _contexts.add(context)
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(BuildServerShutdownMonitor::class.java.name)
        internal val shutdownArgs = listOf(CommandLineArgument("build-server"), CommandLineArgument("shutdown"))
        internal val UseSharedCompilationEnvVarName = "UseSharedCompilation"
        private val buildCommands = setOf(
                DotnetCommandType.Build,
                DotnetCommandType.Pack,
                DotnetCommandType.Publish,
                DotnetCommandType.Test,
                DotnetCommandType.Run,
                DotnetCommandType.MSBuild)
    }
}