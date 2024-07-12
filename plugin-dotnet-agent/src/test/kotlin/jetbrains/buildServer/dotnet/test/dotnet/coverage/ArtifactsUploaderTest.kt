package jetbrains.buildServer.dotnet.test.dotnet.coverage

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_PUBLISH_PATH_PARAM
import jetbrains.buildServer.dotcover.report.artifacts.ArtifactsUploaderImpl
import jetbrains.buildServer.dotcover.report.artifacts.DotnetCoverageArtifactsPublisher
import jetbrains.buildServer.dotcover.report.DotnetCoverageGenerationResult
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.test.utils.TestUtils
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.IOException
import java.util.*

class ArtifactsUploaderTest {

    private lateinit var _mockery: Mockery
    private lateinit var _publisher: DotnetCoverageArtifactsPublisher
    private lateinit var _parameters: DotnetCoverageParameters
    private lateinit var _uploader: ArtifactsUploaderImpl

    @BeforeMethod
    @Throws(Exception::class)
    fun setUp() {
        _mockery = Mockery()
        _parameters = _mockery.mock(DotnetCoverageParameters::class.java)
        _publisher = _mockery.mock(DotnetCoverageArtifactsPublisher::class.java)
        _uploader = ArtifactsUploaderImpl(_publisher)
    }

    @Test
    @Throws(IOException::class)
    fun testPublishSingleReport() {
        val tempFile = TestUtils.myTempFiles.createTempFile()
        val tempDir = TestUtils.myTempFiles.currentTempDir!!
        val result = DotnetCoverageGenerationResult(tempFile, emptyList<File>(), null)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishNamedFile(
                    tempDir,
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "CoverageReport.xml"
                )
            }
        })
        _uploader.processFiles(tempDir, null, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishSingleReport2() {
        val tempFile = TestUtils.myTempFiles.createTempFile()
        val tempDir = TestUtils.myTempFiles.currentTempDir!!
        val result = DotnetCoverageGenerationResult(
            tempFile,
            listOf(TestUtils.myTempFiles.createTempFile(), TestUtils.myTempFiles.createTempFile(), TestUtils.myTempFiles.createTempFile()),
            null
        )
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishNamedFile(
                    tempDir,
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "CoverageReport.xml"
                )
            }
        })
        _uploader.processFiles(tempDir, null, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishMultipleReports() {
        val tempFile1 = TestUtils.myTempFiles.createTempFile()
        val tempFile2 = TestUtils.myTempFiles.createTempFile()
        val tempFile3 = TestUtils.myTempFiles.createTempFile()
        val tempDir = TestUtils.myTempFiles.currentTempDir!!
        val result = DotnetCoverageGenerationResult(null, Arrays.asList(tempFile1, tempFile2, tempFile3), null)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishFile(tempFile1, ".teamcity/.NETCoverage/results")
                oneOf(_publisher).publishFile(tempFile2, ".teamcity/.NETCoverage/results")
                oneOf(_publisher).publishFile(tempFile3, ".teamcity/.NETCoverage/results")
            }
        })
        _uploader.processFiles(tempDir, null, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testCustomFiles() {
        val tempFile1 = TestUtils.myTempFiles.createTempFile()
        val tempFile2 = TestUtils.myTempFiles.createTempFile()
        val tempFile3 = TestUtils.myTempFiles.createTempFile()
        val tempDir = TestUtils.myTempFiles.currentTempDir!!
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), null)
        result.addFileToPublish("a1", tempFile1)
        result.addFileToPublish("a2", tempFile2)
        result.addFileToPublish("b3", tempFile3)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_parameters).getConfigurationParameter(COVERAGE_PUBLISH_PATH_PARAM)
                will(returnValue("myPath"))
                oneOf(_publisher).publishNamedFile(tempDir, tempFile1, "myPath", "a1")
                oneOf(_publisher).publishNamedFile(tempDir, tempFile2, "myPath", "a2")
                oneOf(_publisher).publishNamedFile(tempDir, tempFile3, "myPath", "b3")
            }
        })
        _uploader.processFiles(tempDir, _parameters.getConfigurationParameter(CoverageConstants.COVERAGE_PUBLISH_PATH_PARAM), result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun shouldNotPublishCustomArtifactsWhenCOVERAGE_PUBLISH_PATH_PARAMWasNotDefined() {
        val tempFile1 = TestUtils.myTempFiles.createTempFile()
        val tempFile2 = TestUtils.myTempFiles.createTempFile()
        val tempFile3 = TestUtils.myTempFiles.createTempFile()
        val tempDir = TestUtils.myTempFiles.currentTempDir!!
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), null)
        result.addFileToPublish("a1", tempFile1)
        result.addFileToPublish("a2", tempFile2)
        result.addFileToPublish("b3", tempFile3)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_parameters).getConfigurationParameter(COVERAGE_PUBLISH_PATH_PARAM)
                will(returnValue(null))
                never(_publisher).publishNamedFile(tempDir, tempFile1, "myPath", "a1")
                never(_publisher).publishNamedFile(tempDir, tempFile2, "myPath", "a2")
                never(_publisher).publishNamedFile(tempDir, tempFile3, "myPath", "b3")
            }
        })
        _uploader.processFiles(tempDir, _parameters.getConfigurationParameter(CoverageConstants.COVERAGE_PUBLISH_PATH_PARAM), result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishHTMLReportFile() {
        val tempFile = TestUtils.myTempFiles.createTempFile()
        val tempDir = TestUtils.myTempFiles.currentTempDir!!
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), tempFile)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishNamedFile(
                    tempDir,
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "coverage.zip"
                )
            }
        })
        _uploader.processFiles(tempDir, null, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishHTMLReportDir() {
        val tempFile = TestUtils.myTempFiles.createTempDir()
        val tempDir = TestUtils.myTempFiles.currentTempDir!!
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), tempFile)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishDirectoryZipped(
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "coverage.zip"
                )
            }
        })
        _uploader.processFiles(tempDir, null, result)
        _mockery.assertIsSatisfied()
    }
}
