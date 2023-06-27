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

package jetbrains.buildServer.dotnet.commands.resolution.resolvers

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingFilterType
import kotlinx.coroutines.yield

class TestSuppressTestsSplittingCommandsResolver(
    private val _buildDotnetCommand: DotnetCommand,
    private val _teamCityDotnetToolCommand: DotnetCommand,
    private val _pathService: PathsService,
    private val _testsSplittingSettings: TestsSplittingSettings,
    private val _parameterService: ParametersService,
) : DotnetCommandResolverBase() {
    override val stage = DotnetCommandsResolvingStage.Transformation

    override fun shouldBeApplied(commands: DotnetCommandsStream) =
         _testsSplittingSettings.mode.isSuppressingMode
            && commands.any { it.commandType == DotnetCommandType.Test }

    override fun apply(commands: DotnetCommandsStream) =
        commands.flatMap {
            when (it.commandType) {
                DotnetCommandType.Test -> transform(it)
                else -> sequenceOf(it)
            }
        }

    private fun transform(testCommand: DotnetCommand) = sequence {
        testCommand.targetArguments.forEach { targetArgument ->
            val targetPath = targetArgument.arguments.first().value
            val backupMetadataPath = newBackupMetadataFilePath()
            var binlogPath = newBinlogFilePath()

            // 1. build the target
            yield(BuildWithBinaryLogCommand(_buildDotnetCommand, binlogPath))

            // 2. mutate assemblies by the target path and .binlog file to filter out tests
            yield(SuppressTestsCommand(
                _teamCityDotnetToolCommand,
                sequenceOf(targetPath, binlogPath),
                _testsSplittingSettings.testsClassesFilePath ?: "",
                backupMetadataPath,
                _testsSplittingSettings.filterType == TestsSplittingFilterType.Includes,
            ))

            // 3. test the mutated assemblies by the target path with the original test command
            yield(testCommand)

            // 4. backup to the original assemblies
            yield(RestoreSuppressedTestsCommand(
                _teamCityDotnetToolCommand,
                backupMetadataPath,
            ))
        }
    }

    private fun newBackupMetadataFilePath() = _pathService.getTempFileName(BackupMetadataFileExtension).path

    private fun newBinlogFilePath() = _pathService.getTempFileName(MSBuildBinaryLogFileExtensions).path

    private class BuildWithBinaryLogCommand(
        private val _originalBuildCommand: DotnetCommand,
        private val _binlogPath: String,
    ) : DotnetCommand by _originalBuildCommand {
        override fun getArguments(context: DotnetBuildContext) = sequence {
            // generates MSBuild binary log file (.binlog)
            yield(CommandLineArgument("-bl:LogFile=\"$_binlogPath\""))

            yieldAll(_originalBuildCommand.getArguments(context))
        }
    }

    private class SuppressTestsCommand(
        private val _teamCityDotnetToolCommand: DotnetCommand,
        private val _targetPaths: Sequence<String>,
        private val _testListFilePathArgument: String,
        private val _backupMetadataFilePathArgument: String,
        private val _inclusionMode: Boolean,
    ) : DotnetCommand by _teamCityDotnetToolCommand {
        override val title = "Suppress tests"

        override fun getArguments(context: DotnetBuildContext) = sequence {
            yieldAll(_teamCityDotnetToolCommand.getArguments(context))

            yield(CommandLineArgument("suppress"))

            _targetPaths.forEach {
                yield(CommandLineArgument("--target"))
                yield(CommandLineArgument(it))
            }

            yield(CommandLineArgument("--test-list"))
            yield(CommandLineArgument(_testListFilePathArgument))

            yield(CommandLineArgument("--backup"))
            yield(CommandLineArgument(_backupMetadataFilePathArgument))

            if (_inclusionMode) {
                yield(CommandLineArgument("--inclusion-mode"))
            }
        }
    }

    private class RestoreSuppressedTestsCommand(
        private val _teamCityDotnetToolCommand: DotnetCommand,
        private val _backupMetadataFilePathArgument: String,
    ) : DotnetCommand by _teamCityDotnetToolCommand{
        override val title = "Restore suppressed tests"

        override fun getArguments(context: DotnetBuildContext) = sequence {
            yieldAll(_teamCityDotnetToolCommand.getArguments(context))

            yield(CommandLineArgument("restore"))

            yield(CommandLineArgument("--backup-metadata"))
            yield(CommandLineArgument(_backupMetadataFilePathArgument))
        }
    }

    companion object {
        private const val BackupMetadataFileExtension = ".csv"
        private const val MSBuildBinaryLogFileExtensions = ".binlog"
    }
}