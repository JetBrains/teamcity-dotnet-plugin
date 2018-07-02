package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe

class BuildServerShutdownMonitor(
        _agentLifeCycleEventSources: AgentLifeCycleEventSources,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _dotnetCliToolInfo: DotnetCliToolInfo,
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _pathsService: PathsService)
    : CommandRegistry {

    private var _subscriptionToken: Disposable
    private var _hasBuildCommand = false

    init {
        _subscriptionToken = _agentLifeCycleEventSources.buildFinishedSource.subscribe {
            try {
                if (_hasBuildCommand) {
                    LOG.debug("Has a build command")
                    val version = _dotnetCliToolInfo.version
                    if (version > Version.LastVersionWithoutSharedCompilation) {
                        LOG.debug("$version is greater then ${Version.LastVersionWithoutSharedCompilation}")

                        val buildServerShutdownCommandline = CommandLine(
                                TargetType.Tool,
                                _dotnetToolResolver.executableFile,
                                _pathsService.getPath(PathType.Checkout),
                                shutdownArgs,
                                emptyList())

                        _commandLineExecutor.tryExecute(buildServerShutdownCommandline)
                    }
                }
            } finally {
                _hasBuildCommand = false
            }
        }
    }

    override fun register(dotnetCommandType: DotnetCommandType) {
        if (buildCommands.contains(dotnetCommandType)) {
            _hasBuildCommand = true
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