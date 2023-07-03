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

package jetbrains.buildServer.dotnet.commands.test.splitting

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.utils.getBufferedReader
import java.io.File

class TestsSplittingSettingsImpl(
    private val _parametersService: ParametersService,
    private val _fileSystem: FileSystemService,
) : TestsSplittingSettings {
    override val mode: TestsSplittingMode get() = when {
        !isEnabled -> TestsSplittingMode.Disabled
        isEnabled && useTestSuppressing -> TestsSplittingMode.Suppressing
        isEnabled && useTestNameFilter -> TestsSplittingMode.TestNameFilter
        else -> TestsSplittingMode.TestClassNameFilter
    }

    override val filterType: TestsSplittingFilterType get() =
        when (_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_CURRENT_BATCH)) {
            "1" -> TestsSplittingFilterType.Excludes
            else -> TestsSplittingFilterType.Includes
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

    override val exactMatchFilterSize: Int get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE)
            .let { runCatching { it?.trim()?.toInt() ?: DefaultExactMatchTestsChunkSize } }
            .getOrDefault(DefaultExactMatchTestsChunkSize)

    override val trimTestClassParameters: Boolean get() =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_PARALLEL_TESTS_GROUP_PARAMETRISED_TEST_CLASSES)
            ?.trim()
            ?.toBoolean()
            ?: true

    override val testsClassesFilePath: String? get() =
        filterType
            .let {
                when (it) {
                    TestsSplittingFilterType.Excludes -> DotnetConstants.PARAM_PARALLEL_TESTS_EXCLUDES_FILE
                    TestsSplittingFilterType.Includes -> DotnetConstants.PARAM_PARALLEL_TESTS_INCLUDES_FILE
                }
            }
            .let { _parametersService.tryGetParameter(ParameterType.System, it) }

    private val isEnabled: Boolean get() = testsClassesFilePath != null


    private val useTestSuppressing get() =
        getBoolConfigurationParameter(DotnetConstants.PARAM_PARALLEL_TESTS_USE_SUPPRESSING)

    private val useTestNameFilter get() =
        getBoolConfigurationParameter(DotnetConstants.PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER)

    private val testsClassesFile: Result<File> get() =
        testsClassesFilePath
            ?.let {
                LOG.debug("Tests classes file path in parameters is \"$it\"")
                _fileSystem.getExistingFile(it)
            }
            ?: Result.failure(Error("Cannot find split tests filter file path in parameter"))

    private fun getBoolConfigurationParameter(paramName: String) = _parametersService
        .tryGetParameter(ParameterType.Configuration, paramName)
        ?.trim()
        ?.let { it.equals("true", true) }
        ?: false;

    companion object {
        private val LOG = Logger.getLogger(TestsSplittingSettingsImpl::class.java)
        private const val DefaultExactMatchTestsChunkSize = 10_000
    }
}
