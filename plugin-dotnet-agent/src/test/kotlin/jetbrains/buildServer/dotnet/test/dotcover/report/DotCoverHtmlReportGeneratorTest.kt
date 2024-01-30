package jetbrains.buildServer.dotnet.test.dotcover.report

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotcover.report.DotCoverTeamCityReportGenerator
import jetbrains.buildServer.dotcover.report.model.DotCoverData
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.test.utils.TestUtils
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.StringUtil
import jetbrains.coverage.report.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File
import java.io.IOException
import java.util.*

class DotCoverHtmlReportGeneratorTest {

    @Throws(IOException::class)
    private fun doTest(report: String) {
        val home = File(TestUtils.myTempFiles.createTempDir(), "report.zip")

        val m = Mockery()

        val ps: DotnetCoverageParameters = m.mock(DotnetCoverageParameters::class.java)
        val logger = m.mock(BuildProgressLogger::class.java)

        m.checking(object : Expectations() {
            init {
                allowing(ps).getBuildLogger()
                will(returnValue(logger))
                allowing(ps).resolvePath(".")
                will(returnValue(home))
                allowing(ps).resolvePath("")
                will(returnValue(home))
                allowing(ps).getConfigurationParameter(TestUtils.uninitialized(with(any(String::class.java))))
                will(returnValue(null))
                allowing(ps).getCheckoutDirectory()
                will(returnValue(home))
                allowing(logger).activityStarted(with(any(String::class.java)), with(any(String::class.java)))
                allowing(logger).activityFinished(with(any(String::class.java)), with(any(String::class.java)))
                allowing(logger).warning(with(any(String::class.java)))
            }
        })

        val gne = DotCoverTeamCityReportGenerator()
        gne.generateReportHTMLandStats(ps, ps.resolvePath("."), getTestPath(report), home)

        println("home = $home")

        m.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testReport1() {
        doTest("report5.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_loadModel_1() {
        doLoadModelTest("nreport1.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_loadModel_2() {
        doLoadModelTest("nreport2.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_loadModel_3() {
        doLoadModelTest("nreport3.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_loadModel_4() {
        doLoadModelTest("nreport4.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_loadModel_6() {
        doLoadModelTest("report6.xml")
    }

    @Throws(IOException::class)
    private fun doLoadModelTest(name: String) {
        val data: CoverageData = loadCoverageModel(name)
        val text = coverageDataToString(data)
        compareFiles(name, text)
    }

    @Test
    @Throws(IOException::class)
    fun test_coverageStatsEqual_1() {
        doTestCoverageEqual("nreport1.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_coverageStatsEqual_2() {
        doTestCoverageEqual("nreport2.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_coverageStatsEqual_3() {
        doTestCoverageEqual("nreport3.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_coverageStatsEqual_4() {
        doTestCoverageEqual("nreport4.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_coverageStatsEqual_6() {
        doTestCoverageEqual("report6.xml")
    }

    @Test
    @Throws(IOException::class)
    fun test_coverageReport_shouldParseFilesEvenInNonExistent() {
        val data: DotCoverData = loadCoverageModel("report7.xml")
        val files: Set<File> = data.filesMap.files

        Assert.assertFalse(files.isEmpty())
    }

    @Throws(IOException::class)
    private fun doTestCoverageEqual(name: String) {
        val data: CoverageData = loadCoverageModel(name)
        val calc = ReportBuilderFactory.createStatisticsCalculator()
        calc.compute(data)

        val overallStats = calc.overallStats

        println("overallStats = $overallStats")
        println("parseDotCoverReport" + loadStatementCoverage(name))
    }

    @Throws(IOException::class)
    private fun loadCoverageModel(name: String): DotCoverData {
        val testPath = getTestPath(name)
        val home = TestUtils.myTempFiles.createTempDir()
        val m = Mockery()
        val logger = m.mock(BuildProgressLogger::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(logger).activityStarted(with(any(String::class.java)), with(any(String::class.java)))
                allowing(logger).activityFinished(with(any(String::class.java)), with(any(String::class.java)))
            }
        })
        val gne = DotCoverTeamCityReportGenerator()
        val data: DotCoverData = gne.loadCoverageModel(home, testPath, logger)!!
        m.assertIsSatisfied()
        return data
    }

    @Throws(IOException::class)
    private fun loadStatementCoverage(name: String): Entry {
        val testPath = getTestPath(name)
        val m = Mockery()
        val logger = m.mock(BuildProgressLogger::class.java)
        m.checking(object : Expectations() {
            init {
                allowing(logger).activityStarted(
                    with(
                        any(
                            String::class.java
                        )
                    ), with(any(String::class.java))
                )
                allowing(logger).activityFinished(
                    with(
                        any(
                            String::class.java
                        )
                    ), with(any(String::class.java))
                )
            }
        })
        val gne = DotCoverTeamCityReportGenerator()
        val data: Entry = gne.parseStatementCoverage(testPath)!!
        m.assertIsSatisfied()
        return data
    }

    private fun coverageDataToString(data: CoverageData): String {
        val sb = StringBuilder()
        val classes: List<ClassInfo> = ArrayList(data.classes)
        Collections.sort(
            classes
        ) { o1, o2 ->
            val i = ("" + o1.module).compareTo("" + o2.module)
            if (i == 0) o1.fqName.compareTo(o2.fqName) else i
        }
        for (i in classes) {
            dumpClass("", sb, i)
        }
        return sb.toString()
    }

    private fun dumpClass(prefix: String, sb: StringBuilder, i: ClassInfo) {
        sb.append(prefix)
        sb.append(i.module).append("!  ").append(i.namespace).append("  ").append(i.name).append("\n")
        sb.append(prefix).append("  ->").append(i.fqName).append("\n")
        entry(sb, prefix, "method:", i.methodStats)
        entry(sb, prefix, "block: ", i.blockStats)
        entry(sb, prefix, "line:  ", i.lineStats)
        entry(sb, prefix, "stmt:  ", i.statementStats)
        val innerClasses = i.innerClasses
        if (innerClasses != null) {
            for (inner in innerClasses) {
                dumpClass("$prefix    ", sb, inner)
            }
        }
    }

    private fun entry(sb: StringBuilder, prefix: String, name: String, e: Entry?) {
        if (e == null || e.total == 0 && e.covered == 0) return
        sb.append(prefix).append("    ").append(name).append(":")
        sb.append(e.covered).append("/").append(e.total)
        sb.append("\n")
    }

    companion object {

        fun getTestPath(name: String): File {
            return FileUtil.getCanonicalFile(File(
                DotCoverHtmlReportGeneratorTest::class.java.getResource("../../dotnet/coverage/testData/dotCoverReports/${name}")!!.file))
        }

        @Throws(IOException::class)
        private fun compareFiles(goldFileName: String, actualText: String) {
            val fGold = getTestPath("$goldFileName.txt")
            val tempFile = File(fGold.path + ".tmp")
            FileUtil.writeFileAndReportErrors(tempFile, actualText)
            val sGold = StringUtil.convertLineSeparators(String(FileUtil.loadFileText(fGold)))
            val sActual = StringUtil.convertLineSeparators(actualText)
            Assert.assertEquals(sActual, sGold, "Actual: $sActual")
            FileUtil.delete(tempFile)
        }
    }
}
