package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe

class BuildServerShutdownMonitor(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _dotnetToolResolver: DotnetToolResolver)
    : CommandRegistry {

    private var _subscriptionToken: Disposable
    private var _contexts = mutableListOf<DotnetBuildContext>()

    init {
        _subscriptionToken = agentLifeCycleEventSources.buildFinishedSource.subscribe {
            try {
                if (_contexts.size > 0) {
                    LOG.debug("Has a build command")
                    _contexts.flatMap { it.sdks }.maxBy { it.version }?.let {
                        if (it.version > Version.LastVersionWithoutSharedCompilation) {
                            LOG.debug("$it.version is greater then ${Version.LastVersionWithoutSharedCompilation}")

                            val buildServerShutdownCommandline = CommandLine(
                                    TargetType.Tool,
                                    _dotnetToolResolver.executableFile,
                                    it.path,
                                    shutdownArgs,
                                    emptyList())

                            _commandLineExecutor.tryExecute(buildServerShutdownCommandline)
                        }
                    }
                }
            } finally {
                _contexts.clear()
            }
        }
    }

    override fun register(context: DotnetBuildContext) {
        if (buildCommands.contains(context.command.commandType)) {
            _contexts.add(context)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(BuildServerShutdownMonitor::class.java.name)
        internal val shutdownArgs = listOf(CommandLineArgument("build-server"), CommandLineArgument("shutdown"))
        private val buildCommands = setOf(
                DotnetCommandType.Build,
                DotnetCommandType.Pack,
                DotnetCommandType.Publish,
                DotnetCommandType.Test,
                DotnetCommandType.Run,
                DotnetCommandType.MSBuild)
    }
}