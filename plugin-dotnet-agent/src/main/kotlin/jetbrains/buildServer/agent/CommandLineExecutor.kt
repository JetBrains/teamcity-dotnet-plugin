package jetbrains.buildServer.agent

interface CommandLineExecutor {
    fun tryExecute(
        commandLine: CommandLine,
        executionTimeoutSeconds: Int = 60,
        overrideIdleTimeout: Boolean = false,
    ): CommandLineResult?
}