package jetbrains.buildServer.agent

import jetbrains.buildServer.util.OSType

class WindowsRegistryImpl(
        private val _environment: Environment,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _windowsRegistryParser: WindowsRegistryParser)
    : WindowsRegistry {

    override fun get(key: WindowsRegistryKey, visitor: WindowsRegistryVisitor) {
        if (_environment.os != OSType.WINDOWS) {
            return
        }

        var curKey: WindowsRegistryKey = key;
        for (line in getLines(key)) {
            if (line.isBlank()) {
                continue
            }

            val value = _windowsRegistryParser.tryParseValue(curKey, line)
            if (value != null) {
                if (!visitor.accept(value)) {
                   return
                }
            }
            else {
                val newKey = _windowsRegistryParser.tryParseKey(key, line)
                if (newKey != null) {
                    if (!visitor.accept(newKey)) {
                        return
                    }

                    curKey = newKey;
                }
            }
        }
    }

    private fun getLines(key: WindowsRegistryKey) =
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
            } ?: emptySequence<String>()

    private fun createQueryCommand(vararg args: CommandLineArgument) = CommandLine(
                null,
                TargetType.SystemDiagnostics,
                Path("REG"),
                Path("."),
                listOf(CommandLineArgument("QUERY")) + args)
}