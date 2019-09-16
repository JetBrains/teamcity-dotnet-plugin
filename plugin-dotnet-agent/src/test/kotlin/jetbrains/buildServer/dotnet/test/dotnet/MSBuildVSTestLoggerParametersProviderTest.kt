package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.util.*

class MSBuildVSTestLoggerParametersProviderTest {
    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        EnumSet.of(TestReportingMode.On),
                        listOf(
                                MSBuildParameter("VSTestLogger", "logger://teamcity"),
                                MSBuildParameter("VSTestTestAdapterPath", File("CheckoutDir").absolutePath),
                                MSBuildParameter("VSTestVerbosity", Verbosity.Detailed.id.toLowerCase()))),

                // Supports mult VSTestTestAdapterPath (.NET Core SDK 2.1.102)
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        EnumSet.of(TestReportingMode.MultiAdapterPath),
                        listOf(
                                MSBuildParameter("VSTestLogger", "logger://teamcity"),
                                MSBuildParameter("VSTestTestAdapterPath", "${File("loggerPath").absolutePath};."),
                                MSBuildParameter("VSTestVerbosity", Verbosity.Detailed.id.toLowerCase()))),

                // Reporting is off
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        EnumSet.of(TestReportingMode.Off),
                        emptyList<MSBuildParameter>())
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
            loggerFile: File,
            testReportingMode: EnumSet<TestReportingMode>,
            expectedParameters: List<MSBuildParameter>) {
        // Given
        val ctx = Mockery()
        val context = DotnetBuildContext(File("wd"), ctx.mock(DotnetCommand::class.java))
        val pathsService = ctx.mock(PathsService::class.java)
        val testReportingParameters = ctx.mock(TestReportingParameters::class.java)
        val msBuildVSTestLoggerParameters = ctx.mock(LoggerParameters::class.java)
        val argumentsProvider = MSBuildVSTestLoggerParametersProvider(pathsService, LoggerResolverStub(File("msbuildlogger"), loggerFile), testReportingParameters, msBuildVSTestLoggerParameters)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<TestReportingParameters>(testReportingParameters).getMode(context)
                will(returnValue(testReportingMode))

                oneOf<PathsService>(pathsService).getPath(PathType.Checkout)
                will(returnValue(File("CheckoutDir")))

                oneOf<LoggerParameters>(msBuildVSTestLoggerParameters).vsTestVerbosity
                will(returnValue(Verbosity.Detailed))
            }
        })

        val actualParameters = argumentsProvider.getParameters(context).toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}