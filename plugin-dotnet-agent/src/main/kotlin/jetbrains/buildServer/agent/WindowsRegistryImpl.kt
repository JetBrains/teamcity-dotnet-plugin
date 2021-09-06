package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType

class WindowsRegistryImpl(
        private val _environment: Environment,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _windowsRegistryParser: WindowsRegistryParser)
    : WindowsRegistry {

    override fun accept(key: WindowsRegistryKey, visitor: WindowsRegistryVisitor, recursively: Boolean) {
        LOG.debug("Key: $key")
        LOG.debug("OS: ${_environment.osName}")
        if (_environment.os != OSType.WINDOWS) {
            LOG.debug("Is not Windows OS, finish.")
            return
        }

        if ("Windows XP".equals(_environment.osName, true) && key.bitness != WindowsRegistryBitness.Bitness32) {
            LOG.debug("Skip, because OS is ${_environment.osName} and bitness is ${key.bitness}.")
            return
        }

        LOG.debugBlock("Accepted ${key}").use {
            var curKey = key;
            LOG.debug("Current key is $curKey")
            for (line in getLines(key, recursively)) {
                LOG.debug("Processing line: \"$line\"")
                if (line.isBlank()) {
                    LOG.debug("Skip line: \"$line\"")
                    continue
                }

                val value = _windowsRegistryParser.tryParseValue(curKey, line)
                LOG.debug("Value: $value")
                if (value != null) {
                    if (!visitor.visit(value)) {
                        LOG.debug("Finish")
                        return
                    }
                } else {
                    val newKey = _windowsRegistryParser.tryParseKey(key, line)
                    LOG.debug("New key: $newKey")
                    if (newKey != null) {
                        if (!visitor.visit(newKey)) {
                            LOG.debug("Finish")
                            return
                        }

                        curKey = newKey;
                        LOG.debug("Current key is $curKey")
                    }
                }
            }
        }
    }

    private val supportBittnessFlag get() = !"Windows XP".equals(_environment.osName, true);

    private fun getLines(key: WindowsRegistryKey, recursively: Boolean) =
            _commandLineExecutor.tryExecute(
                    createQueryCommand(
                            sequence {
                                yield(CommandLineArgument(key.regKey))
                                if (supportBittnessFlag) {
                                    yield(CommandLineArgument("/reg:${key.bitness.id}"))
                                }

                                if(recursively) {
                                    yield(CommandLineArgument("/s"))
                                }
                            }

                    ))?.let { result ->
                when (result.exitCode) {
                    0 -> {
                        result.standardOutput.asSequence()
                    }
                    else -> null
                }
            } ?: emptySequence<String>()

    private fun createQueryCommand(args: Sequence<CommandLineArgument>): CommandLine {
        val commandLine =  CommandLine(
                null,
                TargetType.SystemDiagnostics,
                Path("REG"),
                Path("."),
                listOf(CommandLineArgument("QUERY")) + args)

        commandLine.IsInternal = true
        return commandLine
    }

    companion object {
        private val LOG = Logger.getLogger(WindowsRegistryImpl::class.java)
    }
}