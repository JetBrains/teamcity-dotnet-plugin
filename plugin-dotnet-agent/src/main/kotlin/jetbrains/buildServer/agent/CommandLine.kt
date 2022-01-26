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

package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.StdOutText
import java.util.concurrent.atomic.AtomicLong

data class CommandLine(
        val baseCommandLine: CommandLine?,
        val target: TargetType,
        val executableFile: Path,
        val workingDirectory: Path,
        val arguments: List<CommandLineArgument> = emptyList(),
        val environmentVariables: List<CommandLineEnvironmentVariable> = emptyList(),
        val title: String = "",
        val description: List<StdOutText> = emptyList()) {

    public val Id: Long
    public var IsInternal: Boolean = false

    init {
        Id = baseCommandLine?.Id ?: CurrentId.incrementAndGet()
    }

    companion object {
        val CurrentId: AtomicLong = AtomicLong(0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandLine

        if (target != other.target) return false
        if (executableFile != other.executableFile) return false
        if (workingDirectory != other.workingDirectory) return false
        if (arguments != other.arguments) return false
        if (environmentVariables != other.environmentVariables) return false
        if (title != other.title) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = target.hashCode()
        result = 31 * result + executableFile.hashCode()
        result = 31 * result + workingDirectory.hashCode()
        result = 31 * result + arguments.hashCode()
        result = 31 * result + environmentVariables.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }
}

val CommandLine.chain: Sequence<CommandLine> get() {
    var cur: CommandLine? = this
    return sequence {
        while (cur != null) {
            yield(cur!!)
            cur = cur?.baseCommandLine
        }
    }
}