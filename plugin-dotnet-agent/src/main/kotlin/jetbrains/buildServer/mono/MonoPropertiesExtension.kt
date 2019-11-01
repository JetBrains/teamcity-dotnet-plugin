package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.util.EventDispatcher
import org.apache.log4j.Logger
import java.io.File

class MonoPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser)
    : AgentLifeCycleAdapter() {

    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.debug("Locating Mono")
        try {
            val command = CommandLine(
                    null,
                    TargetType.Tool,
                    Path(_toolProvider.getPath(MonoConstants.RUNNER_TYPE)),
                    Path("."),
                    listOf(CommandLineArgument("--version", CommandLineArgumentType.Mandatory)),
                    emptyList())
            _commandLineExecutor.tryExecute(command)?.let {
                val version = _versionParser.parse(it.standardOutput)
                if (version != Version.Empty) {
                    agent.configuration.addConfigurationParameter(MonoConstants.CONFIG_PATH, command.executableFile.path)
                    LOG.info("Found Mono $it at ${command.executableFile.path}")
                }
                else {
                    LOG.info("Mono not found")
                }
            }
        } catch (e: ToolCannotBeFoundException) {
            LOG.info("Mono not found")
            LOG.debug(e)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(MonoPropertiesExtension::class.java)
    }
}