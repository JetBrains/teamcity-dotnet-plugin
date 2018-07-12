package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetBuildContextFactoryTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathsService: PathsService
    private lateinit var _dotnetCliToolInfo: DotnetCliToolInfo
    private lateinit var _parametersService: ParametersService
    private lateinit var _command: DotnetCommand;

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx.mock(PathsService::class.java)
        _dotnetCliToolInfo = _ctx.mock(DotnetCliToolInfo::class.java)
        _parametersService = _ctx.mock(ParametersService::class.java)
        _command = _ctx.mock(DotnetCommand::class.java)
    }

    @DataProvider
    fun testSdkData(): Array<Array<Any>> {
        return arrayOf(
                // Targets were found by relative paths
                arrayOf(
                        sequenceOf(File("dir1", "target1"), File("dir2", "target2")),
                        VirtualFileSystemService()
                                .addDirectory(File("dir1"))
                                .addDirectory(File("dir2")),
                        mapOf(
                                File("dir1") to Version(1, 0, 0),
                                File("dir2") to Version(2, 0, 0)
                        ),
                        setOf(
                                DotnetSdk(CommandLineArgument(File("dir1", "target1").path), File("dir1"), Version(1, 0, 0)),
                                DotnetSdk(CommandLineArgument(File("dir2", "target2").path), File("dir2"), Version(2, 0, 0))
                        )
                ),

                // Targets were found by paths relative to working directory
                arrayOf(
                        sequenceOf(File("dir1", "target1"), File("dir2", "target2")),
                        VirtualFileSystemService()
                                .addDirectory(File("wd", "dir1"))
                                .addDirectory(File("wd", "dir2")),
                        mapOf(
                                File("wd", "dir1") to Version(1, 0, 0),
                                File("wd", "dir2") to Version(2, 0, 0)
                        ),
                        setOf(
                                DotnetSdk(CommandLineArgument(File("dir1", "target1").path), File("wd", "dir1"), Version(1, 0, 0)),
                                DotnetSdk(CommandLineArgument(File("dir2", "target2").path), File("wd", "dir2"), Version(2, 0, 0))
                        )
                ),

                // Mixed targets
                arrayOf(
                        sequenceOf(File("dir1", "target1"), File("dir2", "target2")),
                        VirtualFileSystemService()
                                .addDirectory(File("dir1"))
                                .addDirectory(File("wd", "dir2")),
                        mapOf(
                                File("dir1") to Version(1, 0, 0),
                                File("wd", "dir2") to Version(2, 0, 0)
                        ),
                        setOf(
                                DotnetSdk(CommandLineArgument(File("dir1", "target1").path), File("dir1"), Version(1, 0, 0)),
                                DotnetSdk(CommandLineArgument(File("dir2", "target2").path), File("wd", "dir2"), Version(2, 0, 0))
                        )
                ),

                // One target was not found
                arrayOf(
                        sequenceOf(File("dir1", "target1"), File("dir2", "target2")),
                        VirtualFileSystemService()
                                .addDirectory(File("dir1")),
                        mapOf(
                                File("dir1") to Version(1, 0, 0)
                        ),
                        setOf(
                                DotnetSdk(CommandLineArgument(File("dir1", "target1").path), File("dir1"), Version(1, 0, 0))
                        )
                ),

                // Targets were not found
                arrayOf(
                        sequenceOf(File("dir1", "target1"), File("dir2", "target2")),
                        VirtualFileSystemService(),
                        emptyMap<File, Version>(),
                        emptySet<DotnetSdk>()
                )
        )
    }

    @Test(dataProvider = "testSdkData")
    fun shouldGetDotnetVersionByPath(
            targets: Sequence<File>,
            fileSystemService: FileSystemService,
            versions: Map<File, Version>,
            expectedSdks: Set<DotnetSdk>) {
        // Given
        val workingDir = File("wd")
        _ctx.checking(object : Expectations() {
            init {
                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Normal.id))

                oneOf<DotnetCommand>(_command).targetArguments
                will(returnValue(sequenceOf(TargetArguments(targets.map { CommandLineArgument(it.path) }))))

                allowing<PathsService>(_pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDir))

                for ((path, version) in versions) {
                    oneOf<DotnetCliToolInfo>(_dotnetCliToolInfo).getVersion(path)
                    will(returnValue(version))
                }
            }
        })

        val factory = createInstance(fileSystemService)

        // When
        val actualContext = factory.create(_command)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualContext.sdks, expectedSdks)
    }

    @DataProvider
    fun testVerbosityData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Verbosity.Normal.id, Verbosity.Normal),
                arrayOf(Verbosity.Normal.id.toUpperCase(), Verbosity.Normal),
                arrayOf(Verbosity.Quiet.id, Verbosity.Quiet),
                arrayOf(Verbosity.Minimal.id, Verbosity.Minimal),
                arrayOf(Verbosity.Detailed.id, Verbosity.Detailed),
                arrayOf(Verbosity.Diagnostic.id, Verbosity.Diagnostic),
                arrayOf("abc", null),
                arrayOf("  ", null),
                arrayOf("", null),
                arrayOf<String?>(null, null)
        )
    }

    @Test(dataProvider = "testVerbosityData")
    fun shouldGetVerbosity(
            parameterValue: String?,
            expectedVerbosityLevel: Verbosity?) {
        // Given
        _ctx.checking(object : Expectations() {
            init {
                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(parameterValue))

                oneOf<DotnetCommand>(_command).targetArguments
                will(returnValue(emptySequence<TargetArguments>()))
            }
        })

        val factory = createInstance(VirtualFileSystemService())

        // When
        val actualContext = factory.create(_command)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualContext.verbosityLevel, expectedVerbosityLevel)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetBuildContextFactoryImpl(
                    fileSystemService,
                    _pathsService,
                    _dotnetCliToolInfo,
                    _parametersService)
}