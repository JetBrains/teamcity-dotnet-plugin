package jetbrains.buildServer.dotnet.test.dotnet.coverage

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageGenerationResult
import jetbrains.buildServer.dotnet.coverage.GenerateIndexPagePostProcessor
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.test.utils.TestUtils
import jetbrains.buildServer.util.FileUtil
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.IOException

@Test
class IndexGeneratorProcessorTest {

    private lateinit var _mockery: Mockery
    private lateinit var _home: File
    private lateinit var _logger: BuildProgressLogger
    private var _preferred: String? = null

    @BeforeMethod
    @Throws(Exception::class)
    fun setUp() {
        _mockery = Mockery()
        _home = TestUtils.myTempFiles.createTempDir()
        _preferred = null
        _logger = _mockery.mock(BuildProgressLogger::class.java)
    }

    @Test
    @Throws(IOException::class)
    fun testEmpty() {
        doTest(emptyList())
    }

    @Test
    @Throws(IOException::class)
    fun testIndexHtml() {
        doTest(mutableListOf("index.html"))
    }

    @Test
    @Throws(IOException::class)
    fun testOneFile() {
        doTest(mutableListOf("coverage.html"))
    }

    @Test
    @Throws(IOException::class)
    fun testMoreFiles() {
        doTest(mutableListOf("c", "d", "ov", "er", "a", "ge.ht", "ml"))
    }

    @Test
    @Throws(IOException::class)
    fun test_preferred() {
        val key = "some-foo-report.html"
        _preferred = key
        val myI = File(_home, key)
        FileUtil.writeFileAndReportErrors(myI, "this is true index")
        doTest(mutableListOf("a/b/c", "some.html", "a/another.html", "oops"))
        Assert.assertEquals(myI.length(), File(_home, "index.html").length())
    }

    @Test
    @Throws(IOException::class)
    fun test_preferred_overwrites_index() {
        _mockery.checking(object : Expectations() {
            init {
                atLeast(1).of(_logger).warning(with(any(String::class.java)))
            }
        })
        val key = "some-foo-report.html"
        _preferred = key
        val myI = File(_home, key)
        FileUtil.writeFileAndReportErrors(myI, "this is true index")
        FileUtil.writeFileAndReportErrors(File(_home, "index.html"), "1")
        doTest(mutableListOf("a/b/c", "some.html", "a/another.html", "oops"))
        Assert.assertEquals(myI.length(), File(_home, "index.html").length())
        _mockery.assertIsSatisfied()
    }

    @Throws(IOException::class)
    private fun doTest(fileNames: List<String>) {
        val generatedFiles: MutableMap<File, Long> = HashMap()
        for (fileName in fileNames) {
            val t = File(_home, fileName)
            t.parentFile.mkdirs()
            FileUtil.writeFileAndReportErrors(t, "File: " + fileName + stringOfSize(1023 + generatedFiles.size * 67))
            generatedFiles[t] = t.length()
        }
        val pp = GenerateIndexPagePostProcessor()
        val arb: DotnetCoverageParameters = _mockery.mock(DotnetCoverageParameters::class.java)
        _mockery.checking(object : Expectations() {
            init {
                allowing(arb).getBuildLogger()
                will(returnValue(_logger))
                allowing(arb).getRunnerParameter(CoverageConstants.COVERAGE_HTML_REPORT_INDEX_KEY)
                will(returnValue(_preferred))
            }
        })
        pp.processFiles(arb, DotnetCoverageGenerationResult(null, setOf(TestUtils.myTempFiles.createTempFile(222)), _home))
        for ((key, value) in generatedFiles) {
            Assert.assertTrue(key.exists())
            Assert.assertEquals(key.length(), value)
        }
        Assert.assertTrue(File(_home, "index.html").isFile)
    }

    private fun stringOfSize(sz: Int): String {
        val sb: StringBuilder = StringBuilder(sz + LOREN_IPSUM.length)
        while (sb.length < sz) sb.append(LOREN_IPSUM)
        return sb.toString().substring(0, sz)
    }

    companion object {
        private const val LOREN_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque placerat purus ac leo euismod accumsan in in orci. Nullam mollis, eros id auctor bibendum, est risus dignissim eros, quis semper tortor nisl quis arcu. Nulla mattis accumsan velit, ut consectetur arcu lacinia quis. Maecenas pellentesque justo sed sem hendrerit vel hendrerit augue ullamcorper. Proin congue auctor luctus. In hac habitasse platea dictumst. Sed vestibulum volutpat nulla, nec cursus neque bibendum sed. Vestibulum a feugiat metus. Curabitur vitae lectus neque. Phasellus malesuada eleifend dictum. Maecenas at felis sit amet lacus semper posuere at sodales lorem. Praesent urna nisl, ultricies tristique ornare sit amet, pharetra nec turpis. In ut ligula ac risus dictum malesuada eu et quam."
    }
}
