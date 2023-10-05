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

package jetbrains.buildServer.dotnet.commands.transformation.test

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesSaver
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesSessionManager
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.use

// Transforms a `dotnet test` command to exact match filtered command if needed
// It looks like a sequence of dotnet commands: `dotnet test --list-test` – to get all tests list – and then `dotnet test ...` N times
class TestMethodNameFilterTestSplittingCommandTransformer(
    private val _listTestsDotnetCommand: DotnetCommand,
    private val _testsNamesSessionManager: TestsSplittingByNamesSessionManager,
    private val _loggerService: LoggerService,
) : TestsSplittingCommandTransformer {
    override val mode = TestsSplittingMode.TestNameFilter

    override fun transform(testCommand: DotnetCommand) = sequence {
        _loggerService.writeBlock("dotnet test with filter based on tests names").use {
            _loggerService.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE)
            _testsNamesSessionManager.startSession().use { session ->
                // list all target's tests e.g. `dotnet test --list-tests` single command
                yield(
                    ObservingListTestsDotnetCommand(
                        _listTestsDotnetCommand,
                        ExactMatchListTestsCommandResultHandler(session as TestsSplittingByNamesSaver),
                        testCommand.targetArguments
                    )
                )

                // repeat `dotnet test` commands for every chunk
                yieldAll(session.forEveryTestsNamesChunk { testCommand })
            }
        }
    }

    private class ObservingListTestsDotnetCommand(
        private val _originalCommand: DotnetCommand,
        resultObserver: Observer<CommandResultEvent>,
        override val targetArguments: Sequence<TargetArguments>,
    ) : DotnetCommand by _originalCommand {
        override val resultsObserver = resultObserver
    }

    private class ExactMatchListTestsCommandResultHandler(
        private val _testNamesSaver: TestsSplittingByNamesSaver,
    ) : Observer<CommandResultEvent> {
        private val _whitespacePattern = Regex("\\s+")
        private var _isTestsOutputStarted = false

        override fun onNext(value: CommandResultEvent) {
            if (value !is CommandResultOutput) {
                return
            }

            // we don't want to see millions of tests names in build log
            value.attributes.add(CommandResultAttribute.Suppressed)

            val resultLine = value.output.trim()

            if (!_isTestsOutputStarted) {
                if (resultLine.equals(TestsListOutputMarker, ignoreCase = true)) {
                    _isTestsOutputStarted = true
                }
                return
            }

            resultLine
                .let { _whitespacePattern.split(it) }
                .forEach { _testNamesSaver.tryToSave(it) }
        }

        override fun onError(error: Exception) = Unit

        override fun onComplete() = Unit

        companion object {
            private const val TestsListOutputMarker = "The following Tests are available:"
        }
    }
}