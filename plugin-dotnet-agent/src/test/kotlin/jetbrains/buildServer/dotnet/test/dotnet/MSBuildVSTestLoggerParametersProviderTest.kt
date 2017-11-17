package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.MSBuildVSTestLoggerParametersProvider
import jetbrains.buildServer.dotnet.TestReportingMode
import jetbrains.buildServer.dotnet.TestReportingParameters
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildVSTestLoggerParametersProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        TestReportingMode.On,
                        listOf(MSBuildParameter("VSTestLogger", "logger://teamcity"), MSBuildParameter("VSTestTestAdapterPath", File("CheckoutDir").absolutePath))),

                // Reporting is off
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        TestReportingMode.Off,
                        emptyList<MSBuildParameter>())
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            testReportingMode: TestReportingMode,
            expectedParameters: List<MSBuildParameter>) {
        // Given
        val ctx = Mockery()
        val pathsService = ctx.mock(PathsService::class.java)
        val testReportingParameters = ctx.mock(TestReportingParameters::class.java)
        val argumentsProvider = MSBuildVSTestLoggerParametersProvider(pathsService, LoggerResolverStub(File("msbuildlogger"), loggerFile), testReportingParameters)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<TestReportingParameters>(testReportingParameters).mode
                will(returnValue(testReportingMode))

                oneOf<PathsService>(pathsService).getPath(PathType.Checkout)
                will(returnValue(File("CheckoutDir")))
            }
        })

        val actualParameters = argumentsProvider.parameters.toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}