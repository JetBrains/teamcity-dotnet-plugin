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

package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.agent.Logger
import java.util.*

class SplitTestsNamesManager(
    private val _settings: SplitTestsFilterSettings,
    private val _testListFactory: TestsListFactory,
    private val _langIdentifierValidator: LangIdentifierValidator,
) : SplitTestsNamesSessionManager, SplitTestsNamesSession, SplitTestsNamesSaver, SplitTestsNamesReader {
    private val _testsLists: Queue<TestsList> = LinkedList()
    private val _consideringTestsClasses = mutableSetOf<String>()
    private val _consideringTestsClassesNamespaces = mutableSetOf<String>();

    override fun startSession(): SplitTestsNamesSession {
        // load split test file with test classes names to set to make a fast search by test names prefixes
        // to understand is a specific test should be included/excluded from result filter
        _settings.testClasses.forEach { testClassFQN ->
            _consideringTestsClasses.add(testClassFQN)

            if (!testClassFQN.contains(".")) {
                return@forEach
            }

            var value = testClassFQN
            do {
                value = value.substringBeforeLast('.')
                _consideringTestsClassesNamespaces.add(value)
            } while (value.contains('.'))
        }

        LOG.debug(
            "Loaded ${_consideringTestsClasses.size} test classes names with ${_consideringTestsClassesNamespaces.size} " +
            "namespaces (filter type is ${_settings.filterType}) to make exact match filter for split tests"
        )

        return this
    }

    override fun <T> forEveryTestsNamesChunk(handleChunk: () -> T) = sequence<T> {
        // we should close all writers before read
        _testsLists.forEach { it.dispose() }

        while (!_testsLists.isEmpty()) {
            yield(handleChunk())
            _testsLists.remove()
        }
    }

    override fun tryToSave(testName: String) {
        if (!isValidTestName(testName)) {
            LOG.debug("String \"$testName\" is not a valid test name. This string will be skipped")
            return
        }

        // take only test names from includes/excludes test classes file
        val purposedToSave =
            when (_settings.filterType) {
                SplitTestsFilterType.Includes -> includedInConsideringTestClass(testName)
                SplitTestsFilterType.Excludes -> includedInConsideringTestClass(testName).not()
            }

        if (!purposedToSave) {
            LOG.debug("Test name \"$testName\" isn't purposed to save for filter ${_settings.filterType}. This test name will be skipped")
            return
        }

        LOG.debug("Test name \"$testName\" is purposed to save for filter ${_settings.filterType}. This test name will be saved")
        save(testName)
    }

    override fun read() =
        _testsLists.peek()
            ?.tests
            ?: emptySequence()

    // to close session
    override fun dispose() {
        _testsLists.forEach { it.dispose() }
        _testsLists.clear()
        _consideringTestsClasses.clear()
        _consideringTestsClassesNamespaces.clear()
    }

    private fun isValidTestName(testName: String) =
        _langIdentifierValidator.isValid(testName)
            && !_consideringTestsClassesNamespaces.contains(testName)
            && !_consideringTestsClasses.contains(testName)

    private fun includedInConsideringTestClass(testName: String) =
        _consideringTestsClasses.contains(testName.substringBeforeLast('.'))

    private fun save(testName: String) {
        val testList = when {
            // split to different lists and write in dedicated files
            shouldCreateNewList -> {
                val testList = _testListFactory.new()
                _testsLists.add(testList)
                testList
            }

            else -> _testsLists.last()
        }
        testList.add(testName)
    }

    private val shouldCreateNewList: Boolean get() =
        _testsLists
            .fold(0) { acc, list -> acc + list.testsCount }
            .let { testsCount -> Pair((testsCount + 1) / _settings.exactMatchFilterSize, (testsCount + 1) % _settings.exactMatchFilterSize) }
            .let { (div, rest) ->
                when {
                    div == 0 && rest == 0 -> 0       // 0 / 42 == 0      and     0 % 42 == 0    -->     0 lists
                    div == 0 && rest != 0 -> 1       // 41 / 42 == 0     and     41 % 42 == 41  -->     1 list
                    div != 0 && rest == 0 -> div     // 84 / 42 == 2     and     84 % 42 == 0   -->     2 lists
                    else -> div + 1                  // 85 / 42 == 2     and     85 % 42 == 1   -->     3 lists
                }
            }
            .let { _testsLists.size == 0 || _testsLists.size < it }

    companion object {
        private val LOG = Logger.getLogger(SplitTestsNamesManager::class.java)
    }
}
