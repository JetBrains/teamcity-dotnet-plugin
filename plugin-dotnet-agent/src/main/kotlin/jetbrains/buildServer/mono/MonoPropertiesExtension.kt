

package jetbrains.buildServer.mono

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.config.AgentParametersSupplier
import jetbrains.buildServer.dotnet.MonoConstants


class MonoPropertiesExtension(
        extensionHolder: ExtensionHolder,
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: ToolVersionOutputParser
) : AgentParametersSupplier {

    init {
        extensionHolder.registerExtension(AgentParametersSupplier::class.java, javaClass.name, this)
    }

    override fun getParameters(): MutableMap<String, String> {
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
                if (!version.isEmpty()) {
                    LOG.info("Found Mono $it at ${command.executableFile.path}")
                    return mutableMapOf(Pair(MonoConstants.CONFIG_PATH, command.executableFile.path))
                }
                else {
                    LOG.info("Mono not found")
                }
            }
        } catch (e: ToolCannotBeFoundException) {
            LOG.info("Mono not found")
            LOG.debug(e)
        }

        return mutableMapOf()
    }

    companion object {
        private val LOG = Logger.getLogger(MonoPropertiesExtension::class.java)
    }
}