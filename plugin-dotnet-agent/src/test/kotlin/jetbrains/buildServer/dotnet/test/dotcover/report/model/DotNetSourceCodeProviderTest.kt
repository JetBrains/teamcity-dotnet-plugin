package jetbrains.buildServer.dotnet.test.dotcover.report.model

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotcover.report.model.DotNetSourceCodeProvider
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.test.utils.TestUtils
import jetbrains.buildServer.util.FileUtil
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Invocation
import org.jmock.lib.action.CustomAction
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.IOException

class DotNetSourceCodeProviderTest {

    private lateinit var _mockery: Mockery
    private lateinit var _ps: DotnetCoverageParameters
    private lateinit var _logger: BuildProgressLogger
    private lateinit var _checkoutDir: File
    private lateinit var _foreignDir: File
    private lateinit var _provider: DotNetSourceCodeProvider
    private lateinit var _configurationParameters: MutableMap<String, String>

    @BeforeMethod
    @Throws(Exception::class)
    fun setUp() {
        _mockery = Mockery()
        _configurationParameters = HashMap()
        _checkoutDir = TestUtils.myTempFiles.createTempDir()
        _foreignDir = TestUtils.myTempFiles.createTempDir()
        _provider = DotNetSourceCodeProvider(_checkoutDir)
        _logger = _mockery.mock(BuildProgressLogger::class.java)
        _ps = _mockery.mock(DotnetCoverageParameters::class.java)
        _mockery.checking(object : Expectations() {
            init {
                allowing(_ps).getCheckoutDirectory()
                will(returnValue(_checkoutDir))
                allowing(_ps).getBuildLogger()
                will(returnValue(_logger))
                allowing(_ps).getConfigurationParameter(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("myConfigurationParameters") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any? {
                        return _configurationParameters[invocation.getParameter(0)]
                    }
                })
                allowing(_logger).warning(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("Log warning") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any? {
                        println("WARN-LOG: " + invocation.getParameter(0))
                        return null
                    }
                })
                allowing(_logger).message(TestUtils.uninitialized(with(any(String::class.java))))
                will(object : CustomAction("Log message") {
                    @Throws(Throwable::class)
                    override fun invoke(invocation: Invocation): Any? {
                        println("MESS-LOG: " + invocation.getParameter(0))
                        return null
                    }
                })
            }
        })
    }

    @Test
    @Throws(IOException::class)
    fun testSourcesMapping_local() {

        addExistingFile(1, "foo.txt")
        _provider.preprocessFoundFiles(_logger, _configurationParameters, setOf(1))
        Assert.assertNotNull(_provider.getFileContentLines(1))
    }

    @Test
    @Throws(IOException::class)
    fun testSourcesMapping_notMapped() {
        addForeignFile(1, "foo.txt")
        _provider.preprocessFoundFiles(_logger, _configurationParameters, setOf(1))
        Assert.assertNull(_provider.getFileContentLines(1))
    }

    @Test
    @Throws(IOException::class)
    fun testSourcesMapping_mapped() {
        _configurationParameters["dotNetCoverage.dotCover.source.mapping"] =
            _foreignDir.toString() + "=>" + _checkoutDir
        addForeignFile(1, "foo.txt")
        _provider.preprocessFoundFiles(_logger, _configurationParameters, setOf(1))
        Assert.assertNotNull(_provider.getFileContentLines(1))
    }

    @Test
    @Throws(IOException::class)
    fun testSourcesMapping_mapped_slashes() {
        _configurationParameters["dotNetCoverage.dotCover.source.mapping"] =
            _foreignDir.toString() + "/=>" + _checkoutDir + "/"
        addForeignFile(1, "foo.txt")
        _provider.preprocessFoundFiles(_logger, _configurationParameters, setOf(1))
        Assert.assertNotNull(_provider.getFileContentLines(1))
    }

    @Test
    @Throws(IOException::class)
    fun testSourcesMapping_mapped_trim() {
        _configurationParameters["dotNetCoverage.dotCover.source.mapping"] =
            "  $_foreignDir  =>   $_checkoutDir   "
        addForeignFile(1, "foo.txt")
        _provider.preprocessFoundFiles(_logger, _configurationParameters, setOf(1))
        Assert.assertNotNull(_provider.getFileContentLines(1))
    }

    @Test
    @Throws(IOException::class)
    fun testSourcesMapping_mapped_rel() {
        _configurationParameters["dotNetCoverage.dotCover.source.mapping"] =
            _foreignDir.toString() + "/../aaa/../" + _foreignDir!!.name + "=>" + _checkoutDir + "/"
        addForeignFile(1, "foo.txt")
        _provider.preprocessFoundFiles(_logger, _configurationParameters, setOf(1))
        Assert.assertNotNull(_provider.getFileContentLines(1))
    }

    @Throws(IOException::class)
    private fun addExistingFile(id: Int, name: String): File {
        val path = File(_checkoutDir, name)
        _provider.addFile(id, path)
        FileUtil.writeFileAndReportErrors(path, "this is a file $id, $name")
        return path
    }

    @Throws(IOException::class)
    private fun addForeignFile(id: Int, name: String): File {
        val path = File(_foreignDir, name)
        _provider.addFile(id, path)
        FileUtil.writeFileAndReportErrors(path, "this is a file $id, $name")
        return path
    }
}
