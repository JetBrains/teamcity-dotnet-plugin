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

package jetbrains.buildServer.dotnet.commands.resolution.resolvers.transformation

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetTypeProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingFilterType
import jetbrains.buildServer.rx.use
import java.io.File
import java.nio.file.Paths

class TestSuppressTestsSplittingCommandsResolver(
    private val _buildDotnetCommand: DotnetCommand,
    private val _teamCityDotnetToolCommand: DotnetCommand,
    private val _pathService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _testsSplittingSettings: TestsSplittingSettings,
    private val _loggerService: LoggerService,
    private val _targetTypeProvider: TargetTypeProvider,
) : TestsSplittingCommandsResolverBase(_testsSplittingSettings, _loggerService){
    override val stage = DotnetCommandsResolvingStage.Transformation

    override fun shouldBeApplied(commands: DotnetCommandsStream) =
         _testsSplittingSettings.mode.isSuppressingMode
            && commands.any { it.commandType == DotnetCommandType.Test }

    override fun transform(testCommand: DotnetCommand) = sequence {
        val testsClassesFilePath = _testsSplittingSettings.testsClassesFilePath ?: ""
        if (testsClassesFilePath.isBlank()) {
            "Parallel tests with suppression mode in, however tests classes file path is empty".let {
                LOG.warn(it)
                _loggerService.writeErrorOutput(it)
            }
            return@sequence
        }

        testCommand.targetArguments.forEach forEach@{ targetArguments ->
            if (targetArguments.arguments.filter { it.argumentType == CommandLineArgumentType.Target }.count() == 0) {
                return@forEach
            }

            _loggerService.writeBlock("dotnet test with tests pre-suppression").use {
                _loggerService.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE)
                val backupMetadataPath = newBackupMetadataFilePath()
                var binlogPaths = emptySequence<String>()

                // 1. build only those targets, that could be built
                targetArguments.arguments.map { it.value }
                    .filter { getTargetType(it) != CommandTargetType.Assembly }
                    .forEach { targetPath ->
                        val binlogPath = newBinlogFilePath()
                        binlogPaths += binlogPath

                        yield(BuildWithBinaryLogCommand(_buildDotnetCommand, binlogPath, targetPath))
                    }

                // 2. mutate assemblies by the target paths and MSBuild binary log files to filter out tests
                yield(
                    SuppressTestsCommand(
                        _teamCityDotnetToolCommand,
                        targetArguments.arguments.map { it.value } + binlogPaths,
                        testsClassesFilePath,
                        backupMetadataPath,
                        _testsSplittingSettings.filterType == TestsSplittingFilterType.Includes,
                    )
                )

                // 3. test the mutated assemblies by the target path with the test command that skips the build
                yield(SkipBuildTestCommand(testCommand))

                // 4. backup to the original assemblies if backup file exists
                if (_fileSystemService.isExists(File(backupMetadataPath))) {
                    yield(RestoreSuppressedTestsCommand(_teamCityDotnetToolCommand, backupMetadataPath))
                }
            }
        }
    }

    private fun newBackupMetadataFilePath() = _pathService.getTempFileName(BackupMetadataFileExtension).path

    private fun newBinlogFilePath() = _pathService.getTempFileName(MSBuildBinaryLogFileExtensions).path

    private fun getTargetType(targetPath: String) =
        _targetTypeProvider.getTargetType(Paths.get(targetPath).toFile())

    private class BuildWithBinaryLogCommand(
        private val _originalBuildCommand: DotnetCommand,
        private val _binlogPath: String,
        targetPath: String
    ) : DotnetCommand by _originalBuildCommand {
        override val targetArguments =
            sequenceOf(TargetArguments(sequenceOf(CommandLineArgument(targetPath, CommandLineArgumentType.Target))))

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

            _targetPaths.filter { it.trim() != "" }.forEach {
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

    private class SkipBuildTestCommand(
        private val _originalTestCommand: DotnetCommand,
    ) : DotnetCommand by _originalTestCommand {
        override fun getArguments(context: DotnetBuildContext) = sequence {
            val noBuildArg = CommandLineArgument("--no-build")
            yield(noBuildArg)
            yieldAll(_originalTestCommand.getArguments(context).filter { it != noBuildArg })
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
        private val LOG = Logger.getLogger(TestSuppressTestsSplittingCommandsResolver::class.java)

        private const val BackupMetadataFileExtension = ".csv"
        private const val MSBuildBinaryLogFileExtensions = ".binlog"
    }
}