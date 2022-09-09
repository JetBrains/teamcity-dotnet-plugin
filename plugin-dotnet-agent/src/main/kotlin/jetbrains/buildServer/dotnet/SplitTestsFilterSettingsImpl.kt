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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class SplitTestsFilterSettingsImpl(
    private val _parametersService: ParametersService,
    private val _fileSystem: FileSystemService,
) : SplitTestsFilterSettings {
    override val isActive: Boolean get() = testsClassesFile != null

    override val filterType: SplittedTestsFilterType get() =
        when (_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_CURRENT_BATCH)) {
            "1" -> SplittedTestsFilterType.Excludes
            else -> SplittedTestsFilterType.Includes
        }

    override val testsClassesFile: File? get() =
        filterType
            .let {
                when (it) {
                    SplittedTestsFilterType.Excludes -> DotnetConstants.PARAM_PARALLEL_TESTS_EXCLUDES_FILE
                    SplittedTestsFilterType.Includes -> DotnetConstants.PARAM_PARALLEL_TESTS_INCLUDES_FILE
                }
            }
            .let { _parametersService.tryGetParameter(ParameterType.System, it) }
            ?.let { File(it) }

    override val testClasses: List<String> get() =
        testsClassesFile
            ?.readLinesFromFile()
            ?.map { it.trim() }
            ?.filter { !it.startsWith("#") }
            ?.map { it.trim() }
            ?.filter { it.length > 2 }
            ?: emptyList()

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

    private fun File.readLinesFromFile(): List<String> =
        _fileSystem
            .read(this) { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    val tests: MutableList<String> = ArrayList()
                    while (reader.ready()) {
                        tests += reader.readLine()
                    }
                    tests
                }
            }

    companion object {
        private const val DefaultExactMatchTestsChunkSize = 10_000
    }
}