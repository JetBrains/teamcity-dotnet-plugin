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

import jetbrains.buildServer.BuildProblemTypes.TC_ERROR_MESSAGE_TYPE
import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.rx.Observer

class NugetResultsObserver(
        private val _loggerService: LoggerService)
    : Observer<CommandResultEvent> {
    override fun onNext(value: CommandResultEvent) {
        when(value) {
            is CommandResultOutput -> {
                tryParseByPrefix(value, ErrorPrefix)?.let {
                    value.attributes.add(CommandResultAttribute.Suppressed)
                    if (it.isBlank() || it.startsWith(' ')) {
                        _loggerService.writeErrorOutput(it)
                    } else {
                        _loggerService.writeBuildProblem(it, TC_ERROR_MESSAGE_TYPE, it)
                    }
                }

                tryParseByPrefix(value, WarningPrefix)?.let {
                    value.attributes.add(CommandResultAttribute.Suppressed)
                    _loggerService.writeWarning(it)
                }
            }
        }
    }

    override fun onError(error: Exception) = Unit

    override fun onComplete() = Unit

    private fun tryParseByPrefix(event: CommandResultOutput, prefix: String): String? =
        when {
            event.output.startsWith(prefix) && event.output.length > prefix.length -> event.output.substring(prefix.length, event.output.length)
            else -> null
        }

    companion object {
        internal val ErrorPrefix = "error: "
        internal val WarningPrefix = "warn: "
    }
}