/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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