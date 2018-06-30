package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.ToolResolver
import jetbrains.buildServer.dotnet.VSTestToolResolver
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestToolResolverTest {

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest12Windows.id, "teamcity.dotnet.vstest.12.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest14Windows.id, "teamcity.dotnet.vstest.14.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id, "teamcity.dotnet.vstest.15.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15CrossPlatform.id), File("dotnet"), null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id), File(""), Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: teamcity.dotnet.vstest.15.0")))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideExecutableFile(
            parameters: Map<String, String>,
            expectedExecutableFile: File,
            exceptionPattern: Regex?) {
        // Given
        val instance = createInstance(parameters, File("dotnet"))

        // When
        var actualExecutableFile: File? = null
        try {
            actualExecutableFile = instance.executableFile
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        } catch (ex: RunBuildException) {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true)
        }


        // Then
        if (exceptionPattern == null) {
            Assert.assertEquals(actualExecutableFile, expectedExecutableFile)
        }
    }

    private fun createInstance(parameters: Map<String, String>, executableFile: File): ToolResolver {
        return VSTestToolResolver(ParametersServiceStub(parameters), DotnetToolResolverStub(executableFile, true))
    }
}