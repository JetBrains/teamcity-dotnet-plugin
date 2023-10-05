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

package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments

// Decomposes one `dotnet` command applied to multiple targets
// For example `dotnet test` applied to `Abc.csproj`, `Bcd.csproj` and `Cde.csproj` will be decomposed to 3 commands:
// `dotnet test Abc.csproj`
// `dotnet test Bcd.csproj`
// `dotnet test Cde.csproj`
class MultiTargetDotnetCommandTransformer : DotnetCommandsTransformer {
    override val stage = DotnetCommandsTransformationStage.Targeting

    override fun shouldBeApplied(context: DotnetCommandContext, commands: DotnetCommandsStream) =
        commands.any { it.targetArguments.count() > 1 }

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream) =
        commands
            .flatMap { originalCommand ->
                originalCommand
                    .targetArguments
                    .ifEmpty { sequenceOf(TargetArguments(emptySequence())) }
                    .map { SpecificTargetDotnetCommand(originalCommand, it) }
            }

    class SpecificTargetDotnetCommand(
        private val _originalCommand: DotnetCommand,
        private val _specificTargetArguments: TargetArguments
    ) : DotnetCommand by _originalCommand {
        override val targetArguments: Sequence<TargetArguments>
            get() = sequenceOf(_specificTargetArguments)
    }
}