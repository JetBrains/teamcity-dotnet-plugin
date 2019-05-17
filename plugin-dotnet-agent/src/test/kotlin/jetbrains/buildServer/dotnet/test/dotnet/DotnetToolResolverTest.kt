package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.DotnetToolResolverImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetToolResolverTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathsService: PathsService
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx.mock<ParametersService>(ParametersService::class.java)
    }

    @Test
    fun shouldProvideExecutableFile() {
        // Given
        val instance = createInstance()
        val toolFile = File("dotnet")

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathsService).getToolPath(DotnetConstants.RUNNER_TYPE)
                will(returnValue(toolFile))

                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                will(returnValue(null))
            }
        })

        val actualExecutableFile = instance.executableFile

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualExecutableFile, toolFile)
    }

    @Test
    fun shouldProvideExecutableFileWhenParameterWasOverridedByConfigParameter() {
        // Given
        val instance = createInstance()
        val toolFile = File("dotnet")

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                will(returnValue(toolFile.path))
            }
        })

        val actualExecutableFile = instance.executableFile

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualExecutableFile, toolFile)
    }

    private fun createInstance(): DotnetToolResolver {
        return DotnetToolResolverImpl(_pathsService, _parametersService)
    }
}