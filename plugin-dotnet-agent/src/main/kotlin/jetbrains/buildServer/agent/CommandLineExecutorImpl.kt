package jetbrains.buildServer.agent

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger

class CommandLineExecutorImpl : CommandLineExecutor {
    override fun tryExecute(commandLine: CommandLine, executionTimeoutSeconds: Int): CommandLineResult? {
        val cmd = GeneralCommandLine()
        cmd.exePath = commandLine.executableFile.path
        cmd.setWorkingDirectory(cmd.workDirectory)
        cmd.addParameters(commandLine.arguments.map { it.value })

        val currentEnvironment = System.getenv()
        for ((name, value) in commandLine.environmentVariables) {
            currentEnvironment[name] = value
        }
        currentEnvironment.getOrPut("HOME") { System.getProperty("user.home") }
        cmd.envParams = currentEnvironment

        LOG.info("Execute command line: ${cmd.commandLineString}")
        val executor = jetbrains.buildServer.CommandLineExecutor(cmd)
        return executor.runProcess(executionTimeoutSeconds)?.let {
            val result = CommandLineResult(
                    sequenceOf(it.exitCode),
                    it.outLines.asSequence(),
                    it.stderr.split("\\r?\\n").asSequence())

            if (LOG.isDebugEnabled) {
                val resultStr = StringBuilder()
                resultStr.append("Exit code: ${it.exitCode}")
                resultStr.append("Stdout:\n${it.stdout}")
                resultStr.append("Stderr:\n${it.stderr}")
                LOG.debug("Result:\n$resultStr")
            }
            else {
                LOG.info("Exits with code: ${it.exitCode}")
            }

            return result
        }
    }

    companion object {
        private val LOG = Logger.getInstance(CommandLineExecutorImpl::class.java.name)
    }
}