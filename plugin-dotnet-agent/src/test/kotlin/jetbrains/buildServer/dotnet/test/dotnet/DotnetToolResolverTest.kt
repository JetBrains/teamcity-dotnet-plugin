package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.DotnetToolResolverImpl
import jetbrains.buildServer.agent.runner.PathsService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetToolResolverTest {
    private var _ctx: Mockery? = null
    private var _pathsService: PathsService? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx!!.mock<PathsService>(PathsService::class.java)
    }

    @Test
    fun shouldProvideExecutableFile() {
        // Given
        val instance = createInstance()
        val toolFile = File("dotnet")

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathsService!!).getToolPath(DotnetConstants.RUNNER_TYPE)
                will(returnValue(toolFile))
            }
        })

        val actualExecutableFile = instance.executableFile

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualExecutableFile, toolFile)
    }

    private fun createInstance(): DotnetToolResolver {
        return DotnetToolResolverImpl(_pathsService!!)
    }
}