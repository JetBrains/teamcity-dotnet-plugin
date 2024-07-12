package jetbrains.buildServer.dotnet.test.dotcover.report

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotcover.report.*
import jetbrains.buildServer.dotnet.coverage.SequencerImpl
import jetbrains.buildServer.dotnet.coverage.dotcover.DotCoverParametersFactory
import jetbrains.buildServer.dotnet.coverage.dotcover.DotCoverReportGenerator
import jetbrains.buildServer.dotnet.coverage.dotcover.DotCoverReportRunnerFactory
import jetbrains.buildServer.dotnet.coverage.dotcover.DotCoverVersionFetcher
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.coverage.utils.FileServiceImpl
import jetbrains.buildServer.dotnet.coverage.utils.TempFactoryImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotCoverReportGeneratorTest {
    @MockK private lateinit var _dotCoverReportRunnerFactory: DotCoverReportRunnerFactory;
    private lateinit var _generator: DotCoverReportGenerator

    @BeforeMethod
    @Throws(Exception::class)
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        val dotCoverParametersFactory = DotCoverParametersFactory()
        _generator = DotCoverReportGenerator(
            dotCoverParametersFactory,
            DotCoverTeamCityReportGenerator(),
            DotCoverVersionFetcher(),
            _dotCoverReportRunnerFactory,
            TempFactoryImpl(FileServiceImpl(), SequencerImpl())
        )
    }

    @Test
    fun test_present_no_parameters() {
        val s: String = _generator.presentParameters(mockParameters(Mockery(), null))
        println("s = $s")
    }

    @Test
    fun test_present_parameters() {
        val s: String = _generator.presentParameters(mockParameters(Mockery(), "Fooo"))
        println("s = $s")
    }

    @Test
    fun test_available_no_parameteres() {
        Assert.assertFalse(_generator.supportCoverage(mockParameters(Mockery(), null)))
    }

    @Test
    fun test_available_parameteres() {
        Assert.assertTrue(_generator.supportCoverage(mockParameters(Mockery(), "Foo")))
    }

    @Test
    fun test_equals() {
        val p: DotnetCoverageParameters = mockParameters(Mockery(), "Foo")
        Assert.assertTrue(_generator.parametersEquals(p, p))
    }

    @Test
    fun test_equals2() {
        val p2: DotnetCoverageParameters = mockParameters(Mockery(), "Foo")
        val p1: DotnetCoverageParameters = mockParameters(Mockery(), "Foo")
        Assert.assertTrue(_generator.parametersEquals(p2, p1))
    }

    @Test
    fun test_equals3() {
        val p2: DotnetCoverageParameters = mockParameters(Mockery(), "Foo")
        val p1: DotnetCoverageParameters = mockParameters(Mockery(), "Bao")
        Assert.assertFalse(_generator.parametersEquals(p2, p1))
    }

    @Test
    fun test_equals4() {
        val p2: DotnetCoverageParameters = mockParameters(Mockery(), null)
        val p1: DotnetCoverageParameters = mockParameters(Mockery(), "Bao")
        Assert.assertFalse(_generator.parametersEquals(p2, p1))
    }

    @Test
    fun test_equals5() {
        val p2: DotnetCoverageParameters = mockParameters(Mockery(), null)
        val p1: DotnetCoverageParameters = mockParameters(Mockery(), null)
        Assert.assertTrue(_generator.parametersEquals(p2, p1))
    }

    private fun mockParameters(
        m: Mockery,
        pathToDotCover: String?
    ): DotnetCoverageParameters {
        val cp: DotnetCoverageParameters = m.mock(DotnetCoverageParameters::class.java)
        val logger = m.mock(BuildProgressLogger::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(cp).getBuildLogger()
                will(returnValue(logger))
                allowing(cp).getRunnerParameter("dotNetCoverage.dotCover.home.path")
                will(returnValue(pathToDotCover))
                allowing(cp).makeSnapshot()
                will(returnValue(cp))
            }
        })
        m.checking(object : Expectations() {
            init {
                allowing(logger).warning(with(any(String::class.java)))
                allowing(logger).message(with(any(String::class.java)))
                allowing(logger).activityStarted(with(any(String::class.java)), with(any(String::class.java)))
                allowing(logger).activityFinished(with(any(String::class.java)), with(any(String::class.java)))
            }
        })
        return cp
    }
}
