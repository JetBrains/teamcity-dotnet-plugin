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

class DotnetWorkflowAnalyzerContext {
    private val _commandResults = mutableListOf<Set<CommandResult>>()

    val results: Sequence<Set<CommandResult>> get() = _commandResults.asSequence()

    fun addResult(result: Set<CommandResult>) {
        _commandResults.add(result)
    }
}