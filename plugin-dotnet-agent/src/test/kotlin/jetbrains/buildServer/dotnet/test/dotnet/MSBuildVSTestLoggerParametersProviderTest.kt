package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.WorkflowContext
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.util.*

class MSBuildVSTestLoggerParametersProviderTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _testReportingParameters: TestReportingParameters
    @MockK private lateinit var _msBuildVSTestLoggerParameters: LoggerParameters
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
    }

    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
                // Success scenario
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        EnumSet.of(TestReportingMode.On),
                        listOf(
                                MSBuildParameter("VSTestLogger", "logger://teamcity"),
                                MSBuildParameter("VSTestTestAdapterPath", "v_" + File("checkoutDir").canonicalPath),
                                MSBuildParameter("VSTestVerbosity", Verbosity.Detailed.id.toLowerCase()))),

                // Supports mult VSTestTestAdapterPath (.NET Core SDK 2.1.102)
                arrayOf(
                        File("loggerPath", "vstestlogger.dll") as File?,
                        EnumSet.of(TestReportingMode.MultiAdapterPath),
                        listOf(
                                MSBuildParameter("VSTestLogger", "logger://teamcity"),
                                MSBuildParameter("VSTestTestAdapterPath", "v_" + "${File("loggerPath").canonicalPath};."),
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
        var checkoutDirection =  File("checkoutDir")
        val context = DotnetBuildContext(File("wd"), mockk<DotnetCommand>())
        val argumentsProvider = MSBuildVSTestLoggerParametersProvider(_pathsService, LoggerResolverStub(File("msbuildlogger"), loggerFile), _testReportingParameters, _msBuildVSTestLoggerParameters, _virtualContext)
        every { _testReportingParameters.getMode(context) } returns testReportingMode
        every { _pathsService.getPath(PathType.Checkout) } returns checkoutDirection
        every { _msBuildVSTestLoggerParameters.vsTestVerbosity } returns Verbosity.Detailed

        // When
        val actualParameters = argumentsProvider.getParameters(context).toList()

        // Then
        verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}