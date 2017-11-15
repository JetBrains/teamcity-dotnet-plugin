package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.MSBuildVSTestLoggerParametersProvider
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildVSTestLoggerParametersProviderTest {
    @Test
    fun shouldGetArguments() {
        // Given
        val ctx = Mockery()
        val pathsService = ctx.mock(PathsService::class.java)
        val argumentsProvider = MSBuildVSTestLoggerParametersProvider()

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<PathsService>(pathsService).getPath(PathType.WorkingDirectory)
                will(returnValue(File("wd")))
            }
        })

        val actualParameters = argumentsProvider.parameters.toList()

        // Then
        Assert.assertEquals(actualParameters, listOf(MSBuildParameter("VSTestLogger", "logger://teamcity"), MSBuildParameter("VSTestTestAdapterPath", ".")))
    }
}