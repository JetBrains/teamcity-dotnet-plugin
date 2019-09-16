package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.MonoConstants
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
                    TargetType.Tool,
                    File(_toolProvider.getPath(MonoConstants.RUNNER_TYPE)),
                    File("."),
                    listOf(CommandLineArgument("--version")),
                    emptyList())
            _commandLineExecutor.tryExecute(command)?.let {
                _versionParser.parse(it.standardOutput)?.let {
                    agent.configuration.addConfigurationParameter(MonoConstants.CONFIG_PATH, command.executableFile.absolutePath)
                    LOG.info("Found Mono $it at ${command.executableFile.absolutePath}")
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