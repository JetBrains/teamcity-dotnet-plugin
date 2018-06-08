package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.util.*

class CompilationParametersProviderTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.UNIX, Version(2, 1, 200), listOf(MSBuildParameter("UseSharedCompilation", "false"))),
                arrayOf(OSType.MAC, Version(2, 1, 200), listOf(MSBuildParameter("UseSharedCompilation", "false"))),
                arrayOf(OSType.UNIX, Version(2, 1, 300), listOf(MSBuildParameter("UseSharedCompilation", "false"))),
                arrayOf(OSType.MAC, Version(2, 1, 300), listOf(MSBuildParameter("UseSharedCompilation", "false"))),
                arrayOf(OSType.WINDOWS, Version(2, 1, 200), emptyList<MSBuildParameter>()),
                arrayOf(OSType.WINDOWS, Version(2, 1, 300), emptyList<MSBuildParameter>()),
                arrayOf(OSType.WINDOWS, Version(2, 1, 105), emptyList<MSBuildParameter>()),
                arrayOf(OSType.UNIX, Version(2, 1, 105), emptyList<MSBuildParameter>()),
                arrayOf(OSType.MAC, Version(2, 1, 105), emptyList<MSBuildParameter>()),
                arrayOf(OSType.WINDOWS, Version(2, 0, 0), emptyList<MSBuildParameter>()),
                arrayOf(OSType.UNIX, Version(2, 0, 0), emptyList<MSBuildParameter>()),
                arrayOf(OSType.MAC, Version(2, 0, 0), emptyList<MSBuildParameter>()),
                arrayOf(OSType.WINDOWS, Version(1, 0, 1), emptyList<MSBuildParameter>()),
                arrayOf(OSType.UNIX, Version(1, 0, 1), emptyList<MSBuildParameter>()),
                arrayOf(OSType.MAC, Version(1, 0, 1), emptyList<MSBuildParameter>())
        )
    }

    @Test(dataProvider = "testData")
    fun shouldGetArguments(
            os: OSType,
            version: Version,
            expectedParameters: List<MSBuildParameter>) {
        // Given
        val ctx = Mockery()
        val environment = ctx.mock(Environment::class.java)
        val dotnetCliToolInfo = ctx.mock(DotnetCliToolInfo::class.java)
        val argumentsProvider = CompilationParametersProvider(environment, dotnetCliToolInfo)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(environment).OS
                will(returnValue(os))

                oneOf<DotnetCliToolInfo>(dotnetCliToolInfo).Version
                will(returnValue(version))
            }
        })

        val actualParameters = argumentsProvider.parameters.toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}