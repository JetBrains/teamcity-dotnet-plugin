

package jetbrains.buildServer.dotnet.commands.transformation.test

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetTypeProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingFilterType
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.rx.use
import java.io.File
import java.nio.file.Paths

class TestSuppressTestsSplittingCommandTransformer(
    private val _buildDotnetCommand: DotnetCommand,
    private val _teamCityDotnetToolCommand: DotnetCommand,
    private val _pathService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _testsSplittingSettings: TestsSplittingSettings,
    private val _loggerService: LoggerService,
    private val _targetTypeProvider: TargetTypeProvider,
) : TestsSplittingCommandTransformer {
    override val mode = TestsSplittingMode.Suppression

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
                var hasBuildableTargets = false

                // 1. build only those targets, that could be built
                targetArguments.arguments.map { it.value }
                    .filter { getTargetType(it) != CommandTargetType.Assembly }
                    .forEach { targetPath ->
                        val binlogPath = newBinlogFilePath()
                        binlogPaths += binlogPath
                        hasBuildableTargets = true

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
                yield(if (hasBuildableTargets) SkipBuildTestCommand(testCommand) else testCommand)

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
        private val _targetPath: String
    ) : DotnetCommand by _originalBuildCommand {
        override val targetArguments
            get() = sequenceOf(TargetArguments(sequenceOf(CommandLineArgument(_targetPath, CommandLineArgumentType.Target))))

        override fun getArguments(context: DotnetCommandContext) = sequence {
            // generates MSBuild binary log file (.binlog)
            yield(CommandLineArgument("-bl:LogFile=\"$_binlogPath\""))

            yieldAll(getBuildCommandArguments(context))
        }

        private fun getBuildCommandArguments(context: DotnetCommandContext) =
            _originalBuildCommand.getArguments(context)
                .filter { arg ->
                    // filter out custom arguments (expect -p) explicitly since it's custom arguments
                    // for `dotnet test` command, and might be not compatible with `dotnet build`
                    arg.argumentType != CommandLineArgumentType.Custom || startsWithMSBuildSwitchPrefix(arg.value)
                }

        private fun startsWithMSBuildSwitchPrefix(arg: String): Boolean {
            val unquotedArg = arg.trimStart('"').trimStart('\'').trimStart('`')
            return CustomSwitchesPrefixes.any { unquotedArg.startsWith(it) }
        }

        companion object {
            private val CustomSwitchesPrefixes = arrayOf("-p:", "/p:")
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

        override fun getArguments(context: DotnetCommandContext) = sequence {
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
        override fun getArguments(context: DotnetCommandContext) = sequence {
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

        override fun getArguments(context: DotnetCommandContext) = sequence {
            yieldAll(_teamCityDotnetToolCommand.getArguments(context))

            yield(CommandLineArgument("restore"))

            yield(CommandLineArgument("--backup-metadata"))
            yield(CommandLineArgument(_backupMetadataFilePathArgument))
        }
    }

    companion object {
        private val LOG = Logger.getLogger(TestSuppressTestsSplittingCommandTransformer::class.java)

        private const val BackupMetadataFileExtension = ".csv"
        private const val MSBuildBinaryLogFileExtensions = ".binlog"
    }
}