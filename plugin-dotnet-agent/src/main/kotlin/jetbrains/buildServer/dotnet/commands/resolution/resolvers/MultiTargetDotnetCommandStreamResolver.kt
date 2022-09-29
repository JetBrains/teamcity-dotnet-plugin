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

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandStreamResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage

// Decomposes one `dotnet` command applied to multiple targets
// For example `dotnet test` applied to `Abc.csproj`, `Bcd.csproj` and `Cde.csproj` will be decomposed to 3 commands:
// `dotnet test Abc.csproj`
// `dotnet test Bcd.csproj`
// `dotnet test Cde.csproj`
class MultiTargetDotnetCommandStreamResolver : DotnetCommandStreamResolverBase() {
    override val stage = DotnetCommandsStreamResolvingStage.Targeting

    override fun shouldBeApplied(commands: DotnetCommandsStream) =
        commands.any { it.targetArguments.count() > 1 }

    override fun apply(commands: DotnetCommandsStream) =
        commands
            .flatMap { originalCommand ->
                originalCommand
                    .targetArguments
                    .ifEmpty { sequenceOf(TargetArguments(emptySequence())) }
                    .map { SpecificTargetDotnetCommand(originalCommand, it) }
            }

    class SpecificTargetDotnetCommand constructor(
        private val _originalCommonCommand: DotnetCommand,
        private val _specificTargetArguments: TargetArguments
    ) : DotnetCommand by _originalCommonCommand {
        override val targetArguments: Sequence<TargetArguments>
            get() = sequenceOf(_specificTargetArguments)
    }
}
