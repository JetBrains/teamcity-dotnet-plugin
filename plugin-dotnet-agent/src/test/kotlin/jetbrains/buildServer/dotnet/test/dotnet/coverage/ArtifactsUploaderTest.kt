package jetbrains.buildServer.dotnet.test.dotnet.coverage

import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_PUBLISH_PATH_PARAM
import jetbrains.buildServer.dotnet.coverage.ArtifactsUploaderImpl
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageArtifactsPublisher
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageGenerationResult
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
        val result = DotnetCoverageGenerationResult(tempFile, emptyList<File>(), null)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishNamedFile(
                    _parameters,
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "CoverageReport.xml"
                )
            }
        })
        _uploader.processFiles(_parameters, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishSingleReport2() {
        val tempFile = TestUtils.myTempFiles.createTempFile()
        val result = DotnetCoverageGenerationResult(
            tempFile,
            listOf(TestUtils.myTempFiles.createTempFile(), TestUtils.myTempFiles.createTempFile(), TestUtils.myTempFiles.createTempFile()),
            null
        )
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishNamedFile(
                    _parameters,
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "CoverageReport.xml"
                )
            }
        })
        _uploader.processFiles(_parameters, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishMultipleReports() {
        val tempFile1 = TestUtils.myTempFiles.createTempFile()
        val tempFile2 = TestUtils.myTempFiles.createTempFile()
        val tempFile3 = TestUtils.myTempFiles.createTempFile()
        val result = DotnetCoverageGenerationResult(null, Arrays.asList(tempFile1, tempFile2, tempFile3), null)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishFile(_parameters, tempFile1, ".teamcity/.NETCoverage/results")
                oneOf(_publisher).publishFile(_parameters, tempFile2, ".teamcity/.NETCoverage/results")
                oneOf(_publisher).publishFile(_parameters, tempFile3, ".teamcity/.NETCoverage/results")
            }
        })
        _uploader.processFiles(_parameters, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testCustomFiles() {
        val tempFile1 = TestUtils.myTempFiles.createTempFile()
        val tempFile2 = TestUtils.myTempFiles.createTempFile()
        val tempFile3 = TestUtils.myTempFiles.createTempFile()
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), null)
        result.addFileToPublish("a1", tempFile1)
        result.addFileToPublish("a2", tempFile2)
        result.addFileToPublish("b3", tempFile3)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_parameters).getConfigurationParameter(COVERAGE_PUBLISH_PATH_PARAM)
                will(returnValue("myPath"))
                oneOf(_publisher).publishNamedFile(_parameters, tempFile1, "myPath", "a1")
                oneOf(_publisher).publishNamedFile(_parameters, tempFile2, "myPath", "a2")
                oneOf(_publisher).publishNamedFile(_parameters, tempFile3, "myPath", "b3")
            }
        })
        _uploader.processFiles(_parameters, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun shouldNotPublishCustomArtifactsWhenCOVERAGE_PUBLISH_PATH_PARAMWasNotDefined() {
        val tempFile1 = TestUtils.myTempFiles.createTempFile()
        val tempFile2 = TestUtils.myTempFiles.createTempFile()
        val tempFile3 = TestUtils.myTempFiles.createTempFile()
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), null)
        result.addFileToPublish("a1", tempFile1)
        result.addFileToPublish("a2", tempFile2)
        result.addFileToPublish("b3", tempFile3)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_parameters).getConfigurationParameter(COVERAGE_PUBLISH_PATH_PARAM)
                will(returnValue(null))
                never(_publisher).publishNamedFile(_parameters, tempFile1, "myPath", "a1")
                never(_publisher).publishNamedFile(_parameters, tempFile2, "myPath", "a2")
                never(_publisher).publishNamedFile(_parameters, tempFile3, "myPath", "b3")
            }
        })
        _uploader.processFiles(_parameters, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishHTMLReportFile() {
        val tempFile = TestUtils.myTempFiles.createTempFile()
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), tempFile)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishNamedFile(
                    _parameters,
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "coverage.zip"
                )
            }
        })
        _uploader.processFiles(_parameters, result)
        _mockery.assertIsSatisfied()
    }

    @Test
    @Throws(IOException::class)
    fun testPublishHTMLReportDir() {
        val tempFile = TestUtils.myTempFiles.createTempDir()
        val result = DotnetCoverageGenerationResult(null, emptyList<File>(), tempFile)
        _mockery.checking(object : Expectations() {
            init {
                oneOf(_publisher).publishDirectoryZipped(
                    _parameters,
                    tempFile,
                    ".teamcity/.NETCoverage",
                    "coverage.zip"
                )
            }
        })
        _uploader.processFiles(_parameters, result)
        _mockery.assertIsSatisfied()
    }
}
