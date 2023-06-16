package jetbrains.buildServer.dotnet.test.dotnet.coverage

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.coverage.*
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.test.utils.TestUtils
import org.hamcrest.Description
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Action
import org.jmock.api.Invocation
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class DotnetCoverageReportsMergerTest {

    private lateinit var _mockery: Mockery
    private lateinit var _type: String
    private lateinit var _gen: DotnetCoverageReportGenerator
    private lateinit var _mrg: DotnetCoverageReportsMerger

    @BeforeMethod
    fun setUp() {
        _mockery = Mockery()
        _type = "type"
        _gen = _mockery.mock(DotnetCoverageReportGenerator::class.java)
        _mrg = DotnetCoverageReportsMerger(object : DotnetCoverageReportGeneratorsHolder {
            override fun getReportGenerator(type: String): DotnetCoverageReportGenerator? {
                return if (_type == type) _gen else null
            }
        })
    }

    @Test
    @Throws(IOException::class)
    fun test_simple() {
        val logger = createMockLogger(_mockery, ArrayList())
        val cp1: DotnetCoverageParameters = _mockery.mock(DotnetCoverageParameters::class.java)
        _mockery.checking(object : Expectations() {
            init {
                allowing(cp1).getBuildLogger()
                will(returnValue(logger))
            }
        })
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_gen).supportCoverage(cp1)
                will(returnValue(true))
            }
        })
        val f1 = TestUtils.myTempFiles.createTempFile()
        val r: DotnetCoverageGeneratorInput = _mrg.prepareReports(_type, listOf(DotnetCoverageReportRequest(f1, cp1)))!!
        Assert.assertEquals(r.coverageType, _type)
        Assert.assertEquals(HashSet(r.getFiles()), HashSet(listOf(f1)))
        Assert.assertEquals(r.getFirstStepParameters(), cp1)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun test_merge_reports() {
        val logger = createMockLogger(_mockery, ArrayList())
        val cp1: DotnetCoverageParameters = _mockery.mock(DotnetCoverageParameters::class.java)
        _mockery.checking(object : Expectations() {
            init {
                allowing(cp1).getBuildLogger()
                will(returnValue(logger))
            }
        })
        _mockery.checking(object : Expectations() {
            init {
                exactly(2).of(_gen).supportCoverage(cp1)
                will(returnValue(true))
                oneOf(_gen).parametersEquals(cp1, cp1)
                will(returnValue(true))
            }
        })
        val f1 = TestUtils.myTempFiles.createTempFile()
        val f2 = TestUtils.myTempFiles.createTempFile()
        val r: DotnetCoverageGeneratorInput = _mrg.prepareReports(
            _type,
            listOf(DotnetCoverageReportRequest(f1, cp1), DotnetCoverageReportRequest(f2, cp1))
        )!!
        Assert.assertEquals(r.coverageType, _type)
        Assert.assertEquals(HashSet(r.getFiles()), HashSet(listOf(f1, f2)))
        Assert.assertEquals(r.getFirstStepParameters(), cp1)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun test_merge_diffobj_reports() {
        val logger = createMockLogger(_mockery, ArrayList())
        val cp1: DotnetCoverageParameters = _mockery.mock(DotnetCoverageParameters::class.java, "cp1")
        _mockery.checking(object : Expectations() {
            init {
                allowing(cp1).getBuildLogger()
                will(returnValue(logger))
            }
        })
        val cp2: DotnetCoverageParameters = _mockery.mock(DotnetCoverageParameters::class.java, "cp2")
        _mockery.checking(object : Expectations() {
            init {
                allowing(cp2).getBuildLogger()
                will(returnValue(logger))
            }
        })
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_gen).supportCoverage(cp1)
                will(returnValue(true))
                oneOf(_gen).supportCoverage(cp2)
                will(returnValue(true))
                atMost(1).of(_gen).parametersEquals(cp1, cp1)
                will(returnValue(true))
                atMost(1).of(_gen).parametersEquals(cp2, cp2)
                will(returnValue(true))
                exactly(1).of(_gen).parametersEquals(cp1, cp2)
                will(returnValue(true))
            }
        })
        val f1 = TestUtils.myTempFiles.createTempFile()
        val f2 = TestUtils.myTempFiles.createTempFile()
        val r: DotnetCoverageGeneratorInput = _mrg.prepareReports(
            _type,
            listOf(
                DotnetCoverageReportRequest(f1, cp1),
                DotnetCoverageReportRequest(f2, cp2)
            ))!!
        Assert.assertEquals(r.coverageType, _type)
        Assert.assertEquals(HashSet(r.getFiles()), HashSet(listOf(f1, f2)))
        Assert.assertEquals(r.getFirstStepParameters(), cp1)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun test_merge_diffobj_reports2() {
        val log = ArrayList<String>()
        val logger = createMockLogger(_mockery, log)
        val cp1: DotnetCoverageParameters = _mockery.mock(DotnetCoverageParameters::class.java, "cp1")
        _mockery.checking(object : Expectations() {
            init {
                allowing(cp1).getBuildLogger()
                will(returnValue(logger))
            }
        })
        val cp2: DotnetCoverageParameters = _mockery.mock(DotnetCoverageParameters::class.java, "cp2")
        _mockery.checking(object : Expectations() {
            init {
                allowing(cp2).getBuildLogger()
                will(returnValue(logger))
            }
        })
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_gen).supportCoverage(cp1)
                will(returnValue(true))
                oneOf(_gen).supportCoverage(cp2)
                will(returnValue(true))
                atMost(1).of(_gen).parametersEquals(cp1, cp1)
                will(returnValue(true))
                atMost(1).of(_gen).parametersEquals(cp2, cp2)
                will(returnValue(true))
                oneOf(_gen).parametersEquals(cp1, cp2)
                will(returnValue(false))
                oneOf(_gen).presentParameters(cp1)
                will(returnValue("cp1"))
                oneOf(_gen).presentParameters(cp2)
                will(returnValue("cp2"))
            }
        })
        val f1 = TestUtils.myTempFiles.createTempFile()
        val f2 = TestUtils.myTempFiles.createTempFile()
        val r: DotnetCoverageGeneratorInput = _mrg.prepareReports(
            _type,
            listOf(
                DotnetCoverageReportRequest(f1, cp1),
                DotnetCoverageReportRequest(f2, cp2)
            ))!!
        Assert.assertEquals(r.coverageType, _type)
        Assert.assertEquals(HashSet(r.getFiles()), HashSet(listOf(f1, f2)))
        Assert.assertEquals(r.getFirstStepParameters(), cp1)
        assertStringContained(log, "Will use first parameters to generate all reports")
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun test_empty_reports() {
        Assert.assertNull(_mrg.prepareReports(_type, emptyList()))
    }

    private fun createMockLogger(m: Mockery, sb: MutableList<String>): BuildProgressLogger {
        val logger = m.mock(BuildProgressLogger::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(logger).message(with(any(String::class.java)))
                will(LogToBuilder("message", sb))
                allowing(logger).warning(with(any(String::class.java)))
                will(LogToBuilder("warning", sb))
                allowing(logger).activityStarted(with(any(String::class.java)), with(any(String::class.java)))
                allowing(logger).activityFinished(with(any(String::class.java)), with(any(String::class.java)))
            }
        })
        return logger
    }

    private class LogToBuilder (private val myPrefix: String,
                                private val myMessages: MutableList<String>) : Action {

        @Throws(Throwable::class)
        override fun invoke(invocation: Invocation): Any? {
            val msg = myPrefix + ": " + invocation.getParameter(0)
            myMessages.add(msg)
            println(msg)
            return null
        }

        override fun describeTo(description: Description) {}
    }

    private fun assertStringContained(collection: List<String>, toLook: String) {
        for (s in collection) {
            if (s.contains(toLook)) return
        }
        Assert.fail("Collection: " + ArrayList(collection) + " should contain '" + toLook + "'")
    }
}
