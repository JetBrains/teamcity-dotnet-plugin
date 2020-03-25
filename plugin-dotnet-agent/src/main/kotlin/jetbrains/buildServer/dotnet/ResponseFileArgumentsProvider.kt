/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.rx.use
import org.apache.log4j.Logger
import java.io.OutputStreamWriter

class ResponseFileArgumentsProvider(
        private val _pathsService: PathsService,
        private val _argumentsService: ArgumentsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService,
        private val _msBuildParameterConverter: MSBuildParameterConverter,
        private val _argumentsProviders: List<ArgumentsProvider>,
        private val _parametersProviders: List<MSBuildParametersProvider>,
        private val _virtualContext: VirtualContext,
        private val _msBuildParameterValidator: MSBuildParameterValidator)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val args = _argumentsProviders.flatMap { it.getArguments(context).toList() }
        val params = sequence<MSBuildParameter> {
            for (param in _parametersProviders.flatMap { it.getParameters(context).toList() }) {
                if (_msBuildParameterValidator.isValid(param)) {
                    yield(param)
                } else {
                    LOG.debug("Invalid MSBuild parameter $param.")
                }
            }
        }.toList()


        if (args.isEmpty() && params.isEmpty()) {
            return@sequence
        }

        context.verbosityLevel?.let {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (it) {
                Verbosity.Detailed, Verbosity.Diagnostic -> {
                    _loggerService.writeBlock(BlockName).use {
                        for ((value) in args) {
                            _loggerService.writeStandardOutput(value, Color.Details)
                        }

                        for (param in params) {
                            _loggerService.writeStandardOutput("/p:${param.name}=${param.value}", Color.Details)
                        }
                    }
                }
            }
        }

        val lines = args.map { _argumentsService.normalize(it.value) } + params.map { _msBuildParameterConverter.convert(it) }
        val msBuildResponseFile = _pathsService.getTempFileName(ResponseFileExtension)
        _fileSystemService.write(msBuildResponseFile) {
            OutputStreamWriter(it).use {
                for (line in lines) {
                    it.write("$line\n")
                }
            }
        }

        yield(CommandLineArgument("@${_virtualContext.resolvePath(msBuildResponseFile.path)}", CommandLineArgumentType.Infrastructural))
    }

    companion object {
        private val LOG = Logger.getLogger(ResponseFileArgumentsProvider::class.java)

        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "MSBuild Response File"
        val nodeReuseArgument = CommandLineArgument("/nodeReuse:false", CommandLineArgumentType.Infrastructural)
    }
}