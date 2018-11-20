package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.PathMatcher
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TargetService
import jetbrains.buildServer.dotnet.TargetServiceImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TargetServiceTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathsService: PathsService
    private lateinit var _parametersService: ParametersService
    private lateinit var _argumentsService: ArgumentsService
    private lateinit var _pathMatcher: PathMatcher

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx.mock<ParametersService>(ParametersService::class.java)
        _argumentsService = _ctx.mock<ArgumentsService>(ArgumentsService::class.java)
        _pathMatcher = _ctx.mock<PathMatcher>(PathMatcher::class.java)
    }

    @Test
    fun shouldProvideTargets() {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = sequenceOf("rule1", "rule2", "rule3")

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_PATHS)
                will(returnValue("some includeRules"))

                oneOf<PathsService>(_pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(checkoutDirectory))

                oneOf<ArgumentsService>(_argumentsService).split("some includeRules")
                will(returnValue(includeRules))
            }
        })

        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, includeRules.map { CommandTarget(File(it)) }.toList())
    }

    @Test
    fun shouldExecuteMatcherForWildcards() {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = sequenceOf("rule1", "rule/**/2", "rul?3")
        val expectedRules = sequenceOf("rule1", "rule/a/2", "rule/b/c/2", "rule3")

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_PATHS)
                will(returnValue("some includeRules"))

                oneOf<PathsService>(_pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(checkoutDirectory))

                oneOf<ArgumentsService>(_argumentsService).split("some includeRules")
                will(returnValue(includeRules))

                oneOf<PathMatcher>(_pathMatcher).match(checkoutDirectory, listOf("rule/**/2"))
                will(returnValue(listOf(File("rule/a/2"), File("rule/b/c/2"))))

                oneOf<PathMatcher>(_pathMatcher).match(checkoutDirectory, listOf("rul?3"))
                will(returnValue(listOf(File("rule3"))))
            }
        })

        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, expectedRules.map { CommandTarget(File(it)) }.toList())
    }

    @Test
    fun shouldThrowRunBuildExceptionWhenTargetsWereNotMatched() {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")
        val includeRules = listOf("rule1", "rule/**/2", "rule3")

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_PATHS)
                will(returnValue("some includeRules"))

                oneOf<PathsService>(_pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(checkoutDirectory))

                oneOf<ArgumentsService>(_argumentsService).split("some includeRules")
                will(returnValue(includeRules.asSequence()))

                oneOf<PathMatcher>(_pathMatcher).match(checkoutDirectory, listOf("rule/**/2"))
                will(returnValue(emptyList<CommandTarget>()))
            }
        })

        var actualExceptionWasThrown = false
        try {
            instance.targets.toList()
        } catch (ex: RunBuildException) {
            actualExceptionWasThrown = true
        }

        // Then
        Assert.assertEquals(actualExceptionWasThrown, true)
    }

    @DataProvider
    fun emptyPathsParam(): Array<Array<out String?>> {
        return arrayOf(
                arrayOf(""),
                arrayOf("  "),
                arrayOf(null as String?))
    }

    @Test(dataProvider = "emptyPathsParam")
    fun shouldProvideEmptyTargetsSequenceWhenPathsParamIsEmpty(pathsParam: String?) {
        // Given
        val instance = createInstance()
        val checkoutDirectory = File("checkout")

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_PATHS)
                will(returnValue(pathsParam))

                oneOf<PathsService>(_pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(checkoutDirectory))
            }
        })

        val actualTargets = instance.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, emptyList<CommandTarget>())
    }

    private fun createInstance(): TargetService {
        return TargetServiceImpl(
                _pathsService,
                _parametersService,
                _argumentsService,
                _pathMatcher)
    }
}