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
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesReader

class SplitTestsFilterProvider(
        private val _settings: SplitTestsFilterSettings,
        private val _fileSystem: FileSystemService,
        private val _testsNamesReader: SplitTestsNamesReader,
) : TestsFilterProvider {
    override val filterExpression: String get() =
        _settings.testsClassesFile
            ?.let { testsPartsFile ->
                LOG.debug("Tests group file is \"$testsPartsFile\".")
                if (!_fileSystem.isExists(testsPartsFile) || !_fileSystem.isFile(testsPartsFile)) {
                    LOG.warn("Cannot find file \"$testsPartsFile\".")
                    return@let null
                }

                val filter = buildFilter()

                LOG.debug("Tests group file filter: \"$filter\".")
                filter
            }
            ?: ""

    private fun buildFilter() = when {
        _settings.useExactMatchFilter -> buildExactMatchFilter()
        else -> buildDefaultFilter()
    }

    // FullyQualifiedName~Namespace.TestClass0. | FullyQualifiedName~Namespace.TestClass1. | ...
    // FullyQualifiedName!~Namespace.TestClass0. & FullyQualifiedName!~Namespace.TestClass1. & ...
    private fun buildDefaultFilter(): String {
        val (filterOperation, filterCombineOperator) = when (_settings.filterType) {
            SplittedTestsFilterType.Includes -> Pair("~", " | ")
            SplittedTestsFilterType.Excludes -> Pair("!~", " & ")
        }

        return _settings.testClasses
            .map { "$it." }       // to avoid collisions with overlapping test class names prefixes
            .let { buildFilter("FullyQualifiedName", filterOperation, it, filterCombineOperator) }
    }

    // FullyQualifiedName=Namespace.TestClass0.Test000 | FullyQualifiedName=Namespace.TestClass0.Test001 | ...
    private fun buildExactMatchFilter(): String {
        val (filterOperation, filterCombineOperator) = Pair("=", " | ")

        return _testsNamesReader.read()
            .toList()
            .let { buildFilter("FullyQualifiedName", filterOperation, it, filterCombineOperator) }
    }

    @Suppress("SameParameterValue")
    private fun buildFilter(filterProperty: String, filterOperation: String, filterValues: List<String>, filterCombineOperator: String) =
        // https://docs.microsoft.com/en-us/dotnet/core/testing/selective-unit-tests
        filterValues
            .map { filterValue -> "${filterProperty}${filterOperation}${filterValue}" }
            .let { filterElements ->
                if (filterElements.size > FilterExpressionChunkSize)
                    // chunks in parentheses '(', ')' are necessary to avoid stack overflow in VSTest filter validator
                    // https://youtrack.jetbrains.com/issue/TW-76381
                    filterElements.chunked(FilterExpressionChunkSize) { "(${it.joinToString(filterCombineOperator)})" }
                else
                    filterElements
            }
            .joinToString(filterCombineOperator)

    companion object {
        private val LOG = Logger.getLogger(SplitTestsFilterProvider::class.java)
        private const val FilterExpressionChunkSize = 1000;
    }
}