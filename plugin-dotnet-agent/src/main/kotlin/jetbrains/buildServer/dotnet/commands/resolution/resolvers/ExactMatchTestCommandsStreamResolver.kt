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

package jetbrains.buildServer.dotnet.commands.resolution.resolvers

import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.SplitTestsFilterSettings
import jetbrains.buildServer.dotnet.TargetArguments
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandStreamResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesSaver
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesSessionManager
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.use

// Transforms a `dotnet test` command to exact match filtered command if needed
// It looks like a sequence of dotnet commands: `dotnet test --list-test` – to get all tests list – and then `dotnet test ...` N times
class ExactMatchTestCommandsStreamResolver(
        private val _splitTestsFilterSettings: SplitTestsFilterSettings,
        private val _listTestsDotnetCommand: DotnetCommand,
        private val _testsNamesSessionManager: SplitTestsNamesSessionManager,
) : DotnetCommandStreamResolverBase() {
    override val stage = DotnetCommandsStreamResolvingStage.Transformation

    override fun shouldBeApplied(commands: DotnetCommandsStream) =
        _splitTestsFilterSettings.isActive
            && _splitTestsFilterSettings.useExactMatchFilter
            && commands.any { it.commandType == DotnetCommandType.Test }

    override fun apply(commands: DotnetCommandsStream) =
        commands
            .flatMap {
                when (it.commandType) {
                    DotnetCommandType.Test -> transform(it)
                    else -> sequenceOf(it)
                }
            }

    private fun transform(testCommand: DotnetCommand) = sequence {
        _testsNamesSessionManager.startSession().use { session ->
            // list all target's tests e.g. `dotnet test --list-tests` single command
            yield(ObservingListTestsDotnetCommand(
                _listTestsDotnetCommand,
                ExactMatchListTestsCommandResultHandler(session as SplitTestsNamesSaver),
                testCommand.targetArguments
            ))

            // repeat `dotnet test` commands for every chunk
            yieldAll(session.forEveryTestsNamesChunk { testCommand })
        }
    }

    private class ObservingListTestsDotnetCommand constructor(
        private val _originalCommand: DotnetCommand,
        _resultObserver: Observer<CommandResultEvent>,
        override val targetArguments: Sequence<TargetArguments>,
    ) : DotnetCommand by _originalCommand {
        override val resultsObserver = _resultObserver
    }

    private class ExactMatchListTestsCommandResultHandler(
        private val _testNamesSaver: SplitTestsNamesSaver,
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