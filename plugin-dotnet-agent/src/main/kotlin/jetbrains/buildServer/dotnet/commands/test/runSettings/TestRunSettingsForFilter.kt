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

package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.asSequence
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

class TestRunSettingsForFilter(
    private val _baseTestRunSettingsProvider: TestRunSettingsProvider,
    private val _testsFilterProvider: TestsFilterProvider
)
    : TestRunSettingsProvider {
    override fun tryCreate(command: DotnetCommandType) =
            try {
                _testsFilterProvider.filterExpression
                        .let { it.ifBlank { null } }
                        ?.let { newFilter ->
                            _baseTestRunSettingsProvider.tryCreate(command)?.let { settings ->
                                val testCaseFilter = ensureElementExists(settings, *_testCaseFilterPath)
                                testCaseFilter.textContent = arrayOf(testCaseFilter.textContent, newFilter)
                                        .filter { it.isNotBlank() }
                                        .map { "($it)" }
                                        .joinToString("&")

                                settings
                            }
                        }
            }
            catch (error: Throwable) {
                LOG.error(error)
                null
            }

    private fun ensureElementExists(doc: Document, vararg path: String): Element {
        var node: Node = doc
        for (name in path) {
            node = node.childNodes.asSequence<Element>().filter { name.equals(it.nodeName, true) }.lastOrNull()
                    ?: doc.createElement(name).let {
                        node.appendChild(it)
                        it
                    }
        }

        return node as Element
    }

    companion object {
        private val LOG = Logger.getLogger(TestRunSettingsForFilter::class.java)
        private val _testCaseFilterPath = arrayOf("RunSettings", "RunConfiguration", "TestCaseFilter")
    }
}