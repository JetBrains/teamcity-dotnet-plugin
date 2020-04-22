package jetbrains.buildServer.agent

import jetbrains.buildServer.util.OSType

class WindowsRegistryImpl(
        private val _environment: Environment,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _windowsRegistryParser: WindowsRegistryParser)
    : WindowsRegistry {

    override fun get(key: WindowsRegistryKey, visitor: WindowsRegistryVisitor) {
        var curKey: WindowsRegistryKey = key;
        for (line in getLines(key)) {
            if (line.isBlank()) {
                continue
            }

            val value = _windowsRegistryParser.tryParseValue(curKey, line)
            if (value != null) {
                visitor.accept(value)
            }
            else {
                val newKey = _windowsRegistryParser.tryParseKey(key, line)
                if (newKey != null) {
                    visitor.accept(newKey)
                    curKey = newKey;
                }
            }
        }
    }

    private fun getLines(key: WindowsRegistryKey) =
            when(_environment.os) {
                OSType.WINDOWS -> {
                    _commandLineExecutor.tryExecute(
                            createQueryCommand(
                                    CommandLineArgument(key.regKey),
                                    CommandLineArgument("/reg:${key.bitness.id}"),
                                    CommandLineArgument("/s")
                            ))?.let { result ->
                        when (result.exitCode) {
                            0 -> {
                                result.standardOutput.asSequence()
                            }
                            else -> null
                        }
                    }
                }
                else -> null
            } ?: emptySequence<String>()

    private fun createQueryCommand(vararg args: CommandLineArgument) = CommandLine(
                null,
                TargetType.SystemDiagnostics,
                Path("REG"),
                Path("."),
                listOf(CommandLineArgument("QUERY")) + args)
}