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

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType

class ResponseFileArgumentsProvider(
        private val _responseFileFactory: ResponseFileFactory,
        private val _argumentsProviders: List<ArgumentsProvider>)
    : ArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        val args = _argumentsProviders.flatMap { it.getArguments(context).toList() }

        if (args.isEmpty()) {
            return@sequence
        }

        val responseFile = _responseFileFactory.createResponeFile(
                "",
                args.asSequence(),
                emptySequence<MSBuildParameter>(),
                context.verbosityLevel)

        yield(CommandLineArgument("@${responseFile.path}", CommandLineArgumentType.Infrastructural))
    }
}