/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.StdOutText

data class CommandLine(
        val baseCommandLine: CommandLine?,
        val target: TargetType,
        val executableFile: Path,
        val workingDirectory: Path,
        val arguments: List<CommandLineArgument> = emptyList(),
        val environmentVariables: List<CommandLineEnvironmentVariable> = emptyList(),
        val title: String = "",
        val description: List<StdOutText> = emptyList())

val CommandLine.chain: Sequence<CommandLine> get() {
    var cur: CommandLine? = this
    return sequence {
        while (cur != null) {
            yield(cur!!)
            cur = cur?.baseCommandLine
        }
    }
}