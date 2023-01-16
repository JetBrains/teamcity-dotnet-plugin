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

package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.utils.getBufferedReader
import java.io.File

class SplitTestsFilterSettingsImpl(
    private val _parametersService: ParametersService,
    private val _fileSystem: FileSystemService,
) : SplitTestsFilterSettings {
    override val isActive: Boolean get() = testsClassesFilePath != null

    override val filterType: SplitTestsFilterType
        get() =
        when (_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_CURRENT_BATCH)) {
            "1" -> SplitTestsFilterType.Excludes
            else -> SplitTestsFilterType.Includes
        }

    override val testClasses: Sequence<String> get() = sequence {
        testsClassesFile
            .onFailure {
                LOG.warn("Cannot read tests classes file")
                LOG.warn(it)
            }
            .getOrNull()
            ?.getBufferedReader()
            ?.use { reader ->
                while (reader.ready())
                    yield(reader.readLine())
            }
    }
        .map { it.trim() }
        .filter { !it.startsWith("#") }
        .map { it.trim() }
        .filter { it.length > 2 }

    override val useExactMatchFilter: Boolean get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER)
            ?.trim()
            ?.let { it.equals("true", true) }
            ?: false

    override val exactMatchFilterSize: Int get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE)
            .let { runCatching { it?.trim()?.toInt() ?: DefaultExactMatchTestsChunkSize } }
            .getOrDefault(DefaultExactMatchTestsChunkSize)

    private val testsClassesFilePath: String? get() =
        filterType
            .let {
                when (it) {
                    SplitTestsFilterType.Excludes -> DotnetConstants.PARAM_PARALLEL_TESTS_EXCLUDES_FILE
                    SplitTestsFilterType.Includes -> DotnetConstants.PARAM_PARALLEL_TESTS_INCLUDES_FILE
                }
            }
            .let { _parametersService.tryGetParameter(ParameterType.System, it) }

    private val testsClassesFile: Result<File> get() =
        testsClassesFilePath
            ?.let {
                LOG.debug("Tests classes file path in parameters is \"$it\"")
                _fileSystem.getExistingFile(it)
            }
            ?: Result.failure(Error("Cannot find split tests filter file path in parameter"))

    companion object {
        private val LOG = Logger.getLogger(SplitTestsFilterSettingsImpl::class.java)
        private const val DefaultExactMatchTestsChunkSize = 10_000
    }
}