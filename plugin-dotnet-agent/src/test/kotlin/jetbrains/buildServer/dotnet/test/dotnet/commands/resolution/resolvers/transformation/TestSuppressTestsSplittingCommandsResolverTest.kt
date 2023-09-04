package jetbrains.buildServer.dotnet.test.dotnet.commands.resolution.resolvers.transformation

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.CommandTargetType
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolvingStage
import jetbrains.buildServer.dotnet.commands.resolution.resolvers.transformation.TestSuppressTestsSplittingCommandsResolver
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetTypeProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingFilterType
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.rx.Disposable
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class TestSuppressTestsSplittingCommandsResolverTest {
    @MockK private lateinit var _buildDotnetCommandMock: DotnetCommand
    @MockK private lateinit var _teamCityDotnetToolCommandMock: DotnetCommand
    @MockK private lateinit var _pathServiceMock: PathsService
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _testsSplittingSettingsMock: TestsSplittingSettings
    @MockK private lateinit var _loggerServiceMock: LoggerService
    @MockK private lateinit var _targetTypeProviderMock: TargetTypeProvider
    @MockK private lateinit var _testCommandMock: DotnetCommand

    private lateinit var resolver: TestSuppressTestsSplittingCommandsResolver

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        resolver = TestSuppressTestsSplittingCommandsResolver(
            _buildDotnetCommandMock,
            _teamCityDotnetToolCommandMock,
            _pathServiceMock,
            _fileSystemService,
            _testsSplittingSettingsMock,
            _loggerServiceMock,
            _targetTypeProviderMock,
        )

        justRun { _loggerServiceMock.writeTrace(any()) }
        every { _loggerServiceMock.writeBlock(any()) } returns mockk<Disposable> {
            justRun { dispose() }
        }
    }

    @Test
    fun `should be on Transformation stage`() {
        // arrange, act
        val result = resolver.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsResolvingStage.Transformation)
    }

    @Test
    fun `should not transform and log if tests classes file not present`() {
        // arrange
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.testsClassesFilePath } returns null
        }
        every { _testCommandMock.commandType } returns DotnetCommandType.Test
        justRun { _loggerServiceMock.writeErrorOutput(any()) }

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(0, result.count())
        verify(exactly = 1) { _loggerServiceMock.writeErrorOutput(any()) }
        verify(exactly = 0) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = 0) { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun `should transform to empty command sequence if targets of test command is empty`() {
        // arrange
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.testsClassesFilePath } returns "exclude.txt"
        }
        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.targetArguments } returns emptySequence()
        }

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(0, result.count())
        verify(exactly = 0) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = 0) { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun `should transform to empty command sequence if target argument doesn't contains command line arguments`() {
        // arrange
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.testsClassesFilePath } returns "exclude.txt"
        }
        val targetArguments = mockk<TargetArguments> {
            every { arguments } returns emptySequence()
        }
        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.targetArguments } returns sequenceOf(targetArguments)
        }

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(0, result.count())
        verify(exactly = 0) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = 0) { _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE) }
    }

    @Test
    fun `should transform to 4 commands with suppression in inclusion mode when filter type is inclusion`() {
        // arrange
        val testsListPath = "/path/to/tests-list.txt"
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.filterType } returns TestsSplittingFilterType.Includes
            every { it.testsClassesFilePath } returns testsListPath
        }
        val targetPath = "/path/to/target"
        val targetArguments = sequenceOf(
            mockk<TargetArguments> {
                every { arguments } returns sequenceOf(
                    mockk {
                        every { value } returns targetPath
                        every { argumentType } returns CommandLineArgumentType.Target
                    }
                )
            },
        )
        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.targetArguments } returns targetArguments
        }
        val pathToBackupMetadataCsv = "/path/to/backup-metadata.csv"
        val pathToBinlog = "/path/to/msbuild.binlog"
        _pathServiceMock.let {
            every { it.getTempFileName(".csv") } answers {
                mockk { every { path } returns pathToBackupMetadataCsv }
            }
            every { it.getTempFileName(".binlog") } answers {
                mockk { every { path } returns pathToBinlog }
            }
        }
        every { _targetTypeProviderMock.getTargetType(any()) } returns CommandTargetType.Unknown
        every { _buildDotnetCommandMock.getArguments(any()) } returns emptySequence()
        every { _teamCityDotnetToolCommandMock.getArguments(any()) } returns emptySequence()
        every { _testCommandMock.getArguments(any()) } returns emptySequence()
        every { _fileSystemService.isExists(any()) } returns true

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(result.count(), 4)
        result[0].let { buildCommand ->
            val args = buildCommand.getArguments(mockk())
            Assert.assertEquals(args.count { it.value == "-bl:LogFile=\"$pathToBinlog\""}, 1)
        }
        result[1].let { suppressCommand ->
            val args = suppressCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 10)
            Assert.assertEquals(args[0].value,"suppress")
            Assert.assertEquals(args[1].value,"--target")
            Assert.assertEquals(args[2].value, targetPath)
            Assert.assertEquals(args[3].value,"--target")
            Assert.assertEquals(args[4].value, pathToBinlog)
            Assert.assertEquals(args[5].value,"--test-list")
            Assert.assertEquals(args[6].value, testsListPath)
            Assert.assertEquals(args[7].value,"--backup")
            Assert.assertEquals(args[8].value, pathToBackupMetadataCsv)
            Assert.assertEquals(args[9].value,"--inclusion-mode")
        }
        result[2].let { testCommand ->
            val args = testCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 1)
            Assert.assertEquals(args[0].value,"--no-build")
        }
        result[3].let { restoreCommand ->
            val args = restoreCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 3)
            Assert.assertEquals(args[0].value,"restore")
            Assert.assertEquals(args[1].value,"--backup-metadata")
            Assert.assertEquals(args[2].value, pathToBackupMetadataCsv)
        }
        verify(exactly = targetArguments.count()) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = targetArguments.count()) {
            _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE)
        }
    }

    @Test
    fun `should transform to 4 commands with suppression in exclusion mode when filter type is exclusion`() {
        // arrange
        val testsListPath = "/path/to/tests-list.txt"
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.filterType } returns TestsSplittingFilterType.Excludes
            every { it.testsClassesFilePath } returns testsListPath
        }
        val targetPath = "/path/to/target"
        val targetArguments = sequenceOf(
            mockk<TargetArguments> {
                every { arguments } returns sequenceOf(
                    mockk {
                        every { value } returns targetPath
                        every { argumentType } returns CommandLineArgumentType.Target
                    }
                )
            },
        )
        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.targetArguments } returns targetArguments
        }
        val pathToBackupMetadataCsv = "/path/to/backup-metadata.csv"
        val pathToBinlog = "/path/to/msbuild.binlog"
        _pathServiceMock.let {
            every { it.getTempFileName(".csv") } answers {
                mockk { every { path } returns pathToBackupMetadataCsv }
            }
            every { it.getTempFileName(".binlog") } answers {
                mockk { every { path } returns pathToBinlog }
            }
        }
        every { _targetTypeProviderMock.getTargetType(any()) } returns CommandTargetType.Unknown
        every { _buildDotnetCommandMock.getArguments(any()) } returns emptySequence()
        every { _teamCityDotnetToolCommandMock.getArguments(any()) } returns emptySequence()
        every { _testCommandMock.getArguments(any()) } returns emptySequence()
        every { _fileSystemService.isExists(any()) } returns true

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(result.count(), 4)
        result[0].let { buildCommand ->
            val args = buildCommand.getArguments(mockk())
            Assert.assertEquals(args.count { it.value == "-bl:LogFile=\"$pathToBinlog\""}, 1)
        }
        result[1].let { suppressCommand ->
            val args = suppressCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 9)
            Assert.assertEquals(args[0].value,"suppress")
            Assert.assertEquals(args[1].value,"--target")
            Assert.assertEquals(args[2].value, targetPath)
            Assert.assertEquals(args[3].value,"--target")
            Assert.assertEquals(args[4].value, pathToBinlog)
            Assert.assertEquals(args[5].value,"--test-list")
            Assert.assertEquals(args[6].value, testsListPath)
            Assert.assertEquals(args[7].value,"--backup")
            Assert.assertEquals(args[8].value, pathToBackupMetadataCsv)
        }
        result[2].let { testCommand ->
            val args = testCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 1)
            Assert.assertEquals(args[0].value,"--no-build")
        }
        result[3].let { restoreCommand ->
            val args = restoreCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 3)
            Assert.assertEquals(args[0].value,"restore")
            Assert.assertEquals(args[1].value,"--backup-metadata")
            Assert.assertEquals(args[2].value, pathToBackupMetadataCsv)
        }
        verify(exactly = targetArguments.count()) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = targetArguments.count()) {
            _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE)
        }
    }

    @Test
    fun `should transform to 3 commands (without build) if target is a number of assemblies`() {
        // arrange
        val testsListPath = "/path/to/tests-list.txt"
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.filterType } returns TestsSplittingFilterType.Excludes
            every { it.testsClassesFilePath } returns testsListPath
        }
        val targetPath1 = "/path/to/target1.dll"
        val targetPath2 = "/path/to/target2.dll"
        val targetArguments = sequenceOf(
            mockk<TargetArguments> {
                every { arguments } returns sequenceOf(
                    mockk {
                        every { value } returns targetPath1
                        every { argumentType } returns CommandLineArgumentType.Target
                    },
                    mockk {
                        every { value } returns targetPath2
                        every { argumentType } returns CommandLineArgumentType.Target
                    }
                )
            },
        )
        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.targetArguments } returns targetArguments
        }
        val pathToBackupMetadataCsv = "/path/to/backup-metadata.csv"
        val pathToBinlog = "/path/to/msbuild.binlog"
        _pathServiceMock.let {
            every { it.getTempFileName(".csv") } answers {
                mockk { every { path } returns pathToBackupMetadataCsv }
            }
            every { it.getTempFileName(".binlog") } answers {
                mockk { every { path } returns pathToBinlog }
            }
        }
        every { _targetTypeProviderMock.getTargetType(any()) } returns CommandTargetType.Assembly
        every { _buildDotnetCommandMock.getArguments(any()) } returns emptySequence()
        every { _teamCityDotnetToolCommandMock.getArguments(any()) } returns emptySequence()
        every { _testCommandMock.getArguments(any()) } returns emptySequence()
        every { _fileSystemService.isExists(any()) } returns true

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(result.count(), 3)
        result[0].let { suppressCommand ->
            val args = suppressCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 9)
            Assert.assertEquals(args[0].value,"suppress")
            Assert.assertEquals(args[1].value,"--target")
            Assert.assertEquals(args[2].value, targetPath1)
            Assert.assertEquals(args[3].value,"--target")
            Assert.assertEquals(args[4].value, targetPath2)
            Assert.assertEquals(args[5].value,"--test-list")
            Assert.assertEquals(args[6].value, testsListPath)
            Assert.assertEquals(args[7].value,"--backup")
            Assert.assertEquals(args[8].value, pathToBackupMetadataCsv)
        }
        result[1].let { testCommand ->
            val args = testCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 1)
            Assert.assertEquals(args[0].value,"--no-build")
        }
        result[2].let { restoreCommand ->
            val args = restoreCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 3)
            Assert.assertEquals(args[0].value,"restore")
            Assert.assertEquals(args[1].value,"--backup-metadata")
            Assert.assertEquals(args[2].value, pathToBackupMetadataCsv)
        }
        verify(exactly = targetArguments.count()) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = targetArguments.count()) {
            _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE)
        }
    }

    @Test
    fun `should transform to 3 commands (without restore) when backup metadata doesn't exist`() {
        // arrange
        val testsListPath = "/path/to/tests-list.txt"
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.filterType } returns TestsSplittingFilterType.Includes
            every { it.testsClassesFilePath } returns testsListPath
        }
        val targetPath = "/path/to/target"
        val targetArguments = sequenceOf(
            mockk<TargetArguments> {
                every { arguments } returns sequenceOf(
                    mockk {
                        every { value } returns targetPath
                        every { argumentType } returns CommandLineArgumentType.Target
                    }
                )
            },
        )
        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.targetArguments } returns targetArguments
        }
        val pathToBackupMetadataCsv = "/path/to/backup-metadata.csv"
        val pathToBinlog = "/path/to/msbuild.binlog"
        _pathServiceMock.let {
            every { it.getTempFileName(".csv") } answers {
                mockk { every { path } returns pathToBackupMetadataCsv }
            }
            every { it.getTempFileName(".binlog") } answers {
                mockk { every { path } returns pathToBinlog }
            }
        }
        every { _targetTypeProviderMock.getTargetType(any()) } returns CommandTargetType.Unknown
        every { _buildDotnetCommandMock.getArguments(any()) } returns emptySequence()
        every { _teamCityDotnetToolCommandMock.getArguments(any()) } returns emptySequence()
        every { _testCommandMock.getArguments(any()) } returns emptySequence()
        every { _fileSystemService.isExists(any()) } returns false

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        Assert.assertEquals(result.count(), 3)
        result[0].let { buildCommand ->
            val args = buildCommand.getArguments(mockk())
            Assert.assertEquals(args.count { it.value == "-bl:LogFile=\"$pathToBinlog\""}, 1)
        }
        result[1].let { suppressCommand ->
            val args = suppressCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 10)
            Assert.assertEquals(args[0].value,"suppress")
            Assert.assertEquals(args[1].value,"--target")
            Assert.assertEquals(args[2].value, targetPath)
            Assert.assertEquals(args[3].value,"--target")
            Assert.assertEquals(args[4].value, pathToBinlog)
            Assert.assertEquals(args[5].value,"--test-list")
            Assert.assertEquals(args[6].value, testsListPath)
            Assert.assertEquals(args[7].value,"--backup")
            Assert.assertEquals(args[8].value, pathToBackupMetadataCsv)
            Assert.assertEquals(args[9].value,"--inclusion-mode")
        }
        result[2].let { testCommand ->
            val args = testCommand.getArguments(mockk()).toList()
            Assert.assertEquals(args.count(), 1)
            Assert.assertEquals(args[0].value,"--no-build")
        }
        verify(exactly = targetArguments.count()) { _loggerServiceMock.writeBlock(any()) }
        verify(exactly = targetArguments.count()) {
            _loggerServiceMock.writeTrace(DotnetConstants.PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE)
        }
    }

    @Test
    fun `should transform to sequence of commands including build command with filtered custom arguments`() {
        // arrange
        val testsListPath = "/path/to/tests-list.txt"
        _testsSplittingSettingsMock.let {
            every { it.mode } returns TestsSplittingMode.Suppression
            every { it.filterType } returns TestsSplittingFilterType.Excludes
            every { it.testsClassesFilePath } returns testsListPath
        }
        val targetPath = "/path/to/target"
        val targetArguments = sequenceOf(
            mockk<TargetArguments> {
                every { arguments } returns sequenceOf(
                    mockk {
                        every { value } returns targetPath
                        every { argumentType } returns CommandLineArgumentType.Target
                    }
                )
            },
        )
        _testCommandMock.let {
            every { it.commandType } returns DotnetCommandType.Test
            every { it.targetArguments } returns targetArguments
        }
        val pathToBackupMetadataCsv = "/path/to/backup-metadata.csv"
        val pathToBinlog = "/path/to/msbuild.binlog"
        _pathServiceMock.let {
            every { it.getTempFileName(".csv") } answers {
                mockk { every { path } returns pathToBackupMetadataCsv }
            }
            every { it.getTempFileName(".binlog") } answers {
                mockk { every { path } returns pathToBinlog }
            }
        }
        every { _targetTypeProviderMock.getTargetType(any()) } returns CommandTargetType.Unknown
        every { _teamCityDotnetToolCommandMock.getArguments(any()) } returns emptySequence()
        every { _testCommandMock.getArguments(any()) } returns emptySequence()
        every { _fileSystemService.isExists(any()) } returns true

        every { _buildDotnetCommandMock.getArguments(any()) } returns sequence {
            yield(CommandLineArgument("SHOULD_BE_KEPT", CommandLineArgumentType.Secondary))
            yield(CommandLineArgument("-p:A0=V0", CommandLineArgumentType.Custom))
            yield(CommandLineArgument("/p:A1=V1", CommandLineArgumentType.Custom))
            yield(CommandLineArgument("\'-p:A2=V2\'", CommandLineArgumentType.Custom))
            yield(CommandLineArgument("\"-p:A3=V3\"", CommandLineArgumentType.Custom))
            yield(CommandLineArgument("`-p:A4=V4`", CommandLineArgumentType.Custom))
            yield(CommandLineArgument("SHOULD_BE_FILTERED_OUT", CommandLineArgumentType.Custom))
        }

        // act
        val result = resolver.resolve(sequenceOf(_testCommandMock)).toList()

        // assert
        result[0].let { buildCommand ->
            val args = buildCommand.getArguments(mockk())
            Assert.assertEquals(args.count(), 7)
            Assert.assertEquals(args.count { it.value == "-bl:LogFile=\"$pathToBinlog\""}, 1)
            Assert.assertEquals(args.count { it.value == "SHOULD_BE_KEPT"}, 1)
            Assert.assertEquals(args.count { it.value == "-p:A0=V0"}, 1)
            Assert.assertEquals(args.count { it.value == "/p:A1=V1"}, 1)
            Assert.assertEquals(args.count { it.value == "\'-p:A2=V2\'"}, 1)
            Assert.assertEquals(args.count { it.value == "\"-p:A3=V3\""}, 1)
            Assert.assertEquals(args.count { it.value == "`-p:A4=V4`"}, 1)
            Assert.assertEquals(args.count { it.value == "SHOULD_BE_FILTERED_OUT"}, 0)
        }
    }
}