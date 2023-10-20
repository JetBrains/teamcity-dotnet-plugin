/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class ArgumentsProviderImpl(
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _pluginSpecificationsProvider: PluginsSpecificationProvider
) : ArgumentsProvider {
    override fun getArguments(tool: InspectionTool, toolVersion: Version) =
        getCustomArguments(tool)
            .let { args ->
                val configFileArg = processFileArg(args, ConfigArgRegex, tool.runnerType, ".config")
                val outputFileArg = processFileArg(args, OutputArgRegex, "${tool.toolName}-report", ".xml")
                val logFileArg = processFileArg(args, LogArgRegex, tool.runnerType, ".log")
                val cachesHomeArg = processFileArg(args, CachesHomeArgRegex, tool.runnerType, "")
                    .let {
                        when {
                            it.custom -> it.file
                            else -> _pathsService.getPath(PathType.CachePerCheckout)
                        }
                    }
                val debug = _parametersService.tryGetParameter(ParameterType.Runner, tool.debugSettings) != null || logFileArg.custom
                val extensions = getExtensions(args, tool, toolVersion)

                InspectionArguments(configFileArg.file, outputFileArg.file, logFileArg.file, cachesHomeArg, debug, extensions, args)
            }

    private fun getCustomArguments(tool: InspectionTool) =
        _parametersService.tryGetParameter(ParameterType.Runner, tool.customArgs)
            ?.let {
                it
                    .lineSequence()
                    .filter { it.isNotBlank() }
                    .map { CommandLineArgument(it, CommandLineArgumentType.Custom) }
            }
            ?.toMutableList()
            ?: mutableListOf()

    private fun processFileArg(customArguments: MutableCollection<CommandLineArgument>, regex: Regex, prefix: String, extension: String): FileArg =
        extractCustomArgumentValue(customArguments, regex)
            ?.let { _fileSystemService.createFile(it) }
            ?.let {
                when {
                    !_fileSystemService.isAbsolute(it) -> _fileSystemService.createFile(_pathsService.getPath(PathType.Checkout), it.path)
                    else -> it
                }
            }
            ?.let { FileArg(it, true) }
            ?: FileArg(_fileSystemService.generateTempFile(_pathsService.getPath(PathType.AgentTemp), prefix, extension), false)

    private fun getExtensions(args: MutableList<CommandLineArgument>, tool: InspectionTool, toolVersion: Version): String? {
        return when {
            tool == InspectionTool.Inspectcode && toolVersion >= Version.FirstInspectCodeWithExtensionsOptionVersion -> {
                return processSimpleArgument(args, ExtensionsRegex, _pluginSpecificationsProvider.getPluginsSpecification())
            }

            else -> null
        }
    }

    private fun processSimpleArgument(customArguments: MutableCollection<CommandLineArgument>, regex: Regex, argumentAppendix: String?): String? {
        val customArgument = extractCustomArgumentValue(customArguments, regex)

        return when {
            !customArgument.isNullOrBlank() -> when {
                !argumentAppendix.isNullOrBlank() -> buildString {
                    append(argumentAppendix)
                    append(ArgumentValuePartSeparator)
                    append(customArgument)
                }

                else -> customArgument
            }

            else -> argumentAppendix
        }
    }

    private fun extractCustomArgumentValue(customArguments: MutableCollection<CommandLineArgument>, regex: Regex) =
        tryFindArgumentValue(customArguments, regex)
            ?.let {
                customArguments.remove(it.arg)
                it.value
            }
            ?.trim('\"', '\'')

    private fun tryFindArgumentValue(arguments: Collection<CommandLineArgument>, regex: Regex) =
        arguments.firstNotNullOfOrNull { arg ->
            regex.matchEntire(arg.value)
                ?.groupValues
                ?.get(2)
                ?.let { Arg(arg, it) }
        }

    private data class FileArg(val file: File, val custom: Boolean)

    private data class Arg(val arg: CommandLineArgument, val value: String)

    companion object {
        private val ConfigArgRegex = Regex("(--config)=(.+)", RegexOption.IGNORE_CASE)
        private val OutputArgRegex = Regex("(--output|[-/]o)=(.+)", RegexOption.IGNORE_CASE)
        private val LogArgRegex = Regex("(--logFile)=(.+)", RegexOption.IGNORE_CASE)
        private val CachesHomeArgRegex = Regex("(--caches-home)=(.+)", RegexOption.IGNORE_CASE)
        private val ExtensionsRegex = Regex("(--eXtensions|[-/]x)=(.+)", RegexOption.IGNORE_CASE)
        private const val ArgumentValuePartSeparator = ";"
    }
}