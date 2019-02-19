package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
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
    private lateinit var _command: DotnetCommand

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx.mock(PathsService::class.java)
        _dotnetCliToolInfo = _ctx.mock(DotnetCliToolInfo::class.java)
        _parametersService = _ctx.mock(ParametersService::class.java)
        _command = _ctx.mock(DotnetCommand::class.java)
    }

    @Test
    fun shouldGetDotnetSdkVersionFromWorkingDirectory() {
        // Given
        val workingDir = File("wd")
        val dotnetExecutable = "dotnet"

        _ctx.checking(object : Expectations() {
            init {
                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Normal.id))

                oneOf<PathsService>(_pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDir))

                oneOf<DotnetCliToolInfo>(_dotnetCliToolInfo).getInfo(File(dotnetExecutable), workingDir)
                will(returnValue(
                        DotnetInfo(
                                Version(2, 2, 5),
                                listOf(
                                        DotnetSdk(File("sdk3"), Version(1, 2, 3)),
                                        DotnetSdk(File("sdk4"), Version(2, 2, 5)),
                                        DotnetSdk(File("sdk3"), Version(1, 2, 3))
                                )
                        )
                ))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                will(returnValue(dotnetExecutable))
            }
        })

        val factory = createInstance()

        // When
        val actualContext = factory.create(_command)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(
                actualContext.sdks,
                setOf(
                        DotnetSdk(File("sdk3"), Version(1, 2, 3)),
                        DotnetSdk(File("sdk4"), Version(2, 2, 5))))
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
        val dotnetExecutable = "dotnet"

        _ctx.checking(object : Expectations() {
            init {
                allowing<PathsService>(_pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(File(".")))

                allowing<DotnetCliToolInfo>(_dotnetCliToolInfo).getInfo(File(dotnetExecutable), File("."))
                will(returnValue(DotnetInfo(Version(1, 2, 3), listOf(DotnetSdk(File("sdk"), Version(1, 2, 3))))))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(parameterValue))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                will(returnValue(dotnetExecutable))
            }
        })

        val factory = createInstance()

        // When
        val actualContext = factory.create(_command)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualContext.verbosityLevel, expectedVerbosityLevel)
    }

    private fun createInstance() =
            DotnetBuildContextFactoryImpl(
                    _pathsService,
                    _dotnetCliToolInfo,
                    _parametersService)
}