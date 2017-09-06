package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.TargetService
import jetbrains.buildServer.dotnet.TargetServiceImpl
import jetbrains.buildServer.runners.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class TargetServiceTest {
    private var _ctx: Mockery? = null
    private var _pathsService: PathsService? = null
    private var _parametersService: ParametersService? = null
    private var _argumentsService: ArgumentsService? = null
    private var _pathMatcher: PathMatcher? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx!!.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx!!.mock<ParametersService>(ParametersService::class.java)
        _argumentsService = _ctx!!.mock<ArgumentsService>(ArgumentsService::class.java)
        _pathMatcher = _ctx!!.mock<PathMatcher>(PathMatcher::class.java)
    }

    @Test
    fun shouldProvideTargets() {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = sequenceOf("rule1", "rule2", "rule3")
        val file1 = File("target1")
        val file2 = File("target2")

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_PATHS)
                will(returnValue("some includeRules"))

                oneOf<PathsService>(_pathsService).getPath(PathType.Checkout)
                will(returnValue(checkoutDirectory))

                oneOf<ArgumentsService>(_argumentsService).split("some includeRules")
                will(returnValue(includeRules))

                oneOf<PathMatcher>(_pathMatcher).match(checkoutDirectory, includeRules, emptySequence())
                will(returnValue(sequenceOf(file1, file2)))
            }
        })

        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(
                actualTargets,
                listOf(CommandTarget(file1), CommandTarget(file2)))
    }

    private fun createInstance(): TargetService {
        return TargetServiceImpl(_pathsService!!, _parametersService!!, _argumentsService!!, _pathMatcher!!)
    }
}