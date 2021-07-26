package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType

class WindowsRegistryImpl(
        private val _environment: Environment,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _windowsRegistryParser: WindowsRegistryParser)
    : WindowsRegistry {

    override fun accept(key: WindowsRegistryKey, visitor: WindowsRegistryVisitor, recursively: Boolean) {
        if (_environment.os != OSType.WINDOWS) {
            return
        }

        LOG.debugBlock("Accepted ${key}").use {
            var curKey: WindowsRegistryKey = key;
            for (line in getLines(key, recursively)) {
                if (line.isBlank()) {
                    continue
                }

                val value = _windowsRegistryParser.tryParseValue(curKey, line)
                if (value != null) {
                    if (!visitor.visit(value)) {
                        return
                    }
                } else {
                    val newKey = _windowsRegistryParser.tryParseKey(key, line)
                    if (newKey != null) {
                        if (!visitor.visit(newKey)) {
                            return
                        }

                        curKey = newKey;
                    }
                }
            }
        }
    }

    private fun getLines(key: WindowsRegistryKey, recursively: Boolean) =
            _commandLineExecutor.tryExecute(
                    createQueryCommand(
                            sequence {
                                yield(CommandLineArgument(key.regKey))
                                yield(CommandLineArgument("/reg:${key.bitness.id}"))
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