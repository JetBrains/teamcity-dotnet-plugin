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

import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.StdOutText
import jetbrains.buildServer.util.OSType
import java.io.File

class CommandLinePresentationServiceImpl(
        private val _environment: Environment,
        private val _argumentsService: ArgumentsService,
        private val _virtualContext: VirtualContext)
    : CommandLinePresentationService {
    override fun buildExecutablePresentation(executableFile: Path): List<StdOutText> {
        val executableFilePath = _argumentsService.normalize(executableFile.path)
        val output = mutableListOf<StdOutText>()
        val lastSeparatorIndex = executableFilePath.indexOfLast { it == File.separatorChar || it == '\\' || it == '/' }
        if (lastSeparatorIndex >= 0) {
            output.add(StdOutText(executableFilePath.substring(0 .. lastSeparatorIndex - 1) + separatorChar))
        }

        output.add(StdOutText(executableFilePath.substring(lastSeparatorIndex + 1)))
        return output
    }

    override fun buildArgsPresentation(arguments: List<CommandLineArgument>): List<StdOutText> =
            arguments.map {
                StdOutText(
                        " ${_argumentsService.normalize(it.value)}",
                        when (it.argumentType) {
                            CommandLineArgumentType.Mandatory -> Color.Default
                            CommandLineArgumentType.Target -> Color.Default
                            CommandLineArgumentType.Custom -> Color.Default
                            CommandLineArgumentType.Infrastructural -> Color.Default
                            else -> Color.Default
                        }
                )
            }

    private val separatorChar get() = when {
        _environment.os == _virtualContext.targetOSType -> File.separatorChar
        _virtualContext.targetOSType == OSType.WINDOWS -> '\\'
        else -> '/'
    }
}