

package jetbrains.buildServer.agent

import com.intellij.execution.configurations.GeneralCommandLine
import java.io.File

class CommandLineExecutorImpl : CommandLineExecutor {
    override fun tryExecute(commandLine: CommandLine, executionTimeoutSeconds: Int): CommandLineResult? {
        val cmd = GeneralCommandLine()
        cmd.exePath = commandLine.executableFile.path
        cmd.setWorkingDirectory(File(commandLine.workingDirectory.path))
        cmd.addParameters(commandLine.arguments.map { it.value })

        val currentEnvironment = System.getenv().toMutableMap()
        for ((name, value) in commandLine.environmentVariables) {
            currentEnvironment[name] = value
        }

        currentEnvironment.getOrPut("HOME") { System.getProperty("user.home") }
        cmd.envParams = currentEnvironment

        val executor = jetbrains.buildServer.CommandLineExecutor(cmd)
        return executor.runProcess(executionTimeoutSeconds)?.let {
            if (LOG.isDebugEnabled) {
                LOG.debug("---> \"${cmd.commandLineString}\"}")
            }

            val result = CommandLineResult(
                    it.exitCode,
                    it.outLines.toList(),
                    it.stderr.split("\\r?\\n").toList())

            if (LOG.isDebugEnabled) {
                val resultStr = StringBuilder()
                resultStr.append("<--- Exit code: ${it.exitCode}")
                resultStr.append("<--- Stdout:\n${it.stdout}")
                resultStr.append("<--- Stderr:\n${it.stderr}")
                LOG.debug("<--- Result:\n$resultStr")
            } else {
                val message = "<--- \"${cmd.commandLineString}\" exits with code: ${it.exitCode}"
                if(commandLine.IsInternal) {
                    LOG.debug(message)
                }
                else {
                    LOG.info(message)
                }
            }

            return result
        }
    }

    companion object {
        private val LOG = Logger.getLogger(CommandLineExecutorImpl::class.java)
    }
}