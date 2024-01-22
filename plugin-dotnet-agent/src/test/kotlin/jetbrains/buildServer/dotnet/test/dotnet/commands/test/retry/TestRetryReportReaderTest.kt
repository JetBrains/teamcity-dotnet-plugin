package jetbrains.buildServer.dotnet.test.dotnet.commands.test.retry

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryReportReader
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetrySettings
import jetbrains.buildServer.utils.getBufferedReader
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.BufferedReader
import java.io.File

class TestRetryReportReaderTest {
    @MockK
    private lateinit var _fileSystemMock: FileSystemService

    @MockK
    private lateinit var _retrySettingsMock: TestRetrySettings

    @MockK
    private lateinit var _file1Reader: BufferedReader

    @MockK
    private lateinit var _file2Reader: BufferedReader

    private lateinit var _reader: TestRetryReportReader

    private val _directoryPath = File("/path/to/reports/")
    private val _file1Path = File("/path/to/reports/1.txt")
    private val _file2Path = File("/path/to/reports/2.txt")

    @BeforeClass
    fun beforeAll() = MockKAnnotations.init(this)

    @BeforeMethod
    fun setup() {
        mockkStatic(File::getBufferedReader)
        clearAllMocks()

        _fileSystemMock.let {
            every { it.isExists(_directoryPath) } returns true
            every { it.isDirectory(_directoryPath) } returns true
            every { it.list(_directoryPath) } returns sequenceOf(_file1Path, _file2Path)
            every { it.isFile(_file1Path) } returns true
            every { it.isFile(_file2Path) } returns true
        }

        every { _file1Path.getBufferedReader() } returns _file1Reader
        justRun { _file1Reader.close() }

        every { _file2Path.getBufferedReader() } returns _file2Reader
        justRun { _file2Reader.close() }

        _retrySettingsMock.let {
            every { it.reportPath } returns _directoryPath.path
            every { it.maxFailures } returns 1000
        }

        _reader = TestRetryReportReader(_retrySettingsMock, _fileSystemMock)
    }

    @Test
    fun `should read unique test names from all files`() {
        // arrange
        every { _file1Reader.readLine() } returnsMany listOf("test1", "test2", null)
        every { _file2Reader.readLine() } returnsMany listOf("test2", "test3", "test4", null)

        // act
        val result = _reader.readFailedTestNames()

        // assert
        Assert.assertEquals(result.size, 4)
        verify(exactly = 1) { _file1Reader.close() }
        verify(exactly = 1) { _file2Reader.close() }
    }

    @Test
    fun `should not read more test names than maximum allowed tests to retry`() {
        // arrange
        val testRetryThreshold = 1000
        every { _retrySettingsMock.maxFailures } returns testRetryThreshold

        every { _file1Reader.readLine() } returnsMany MutableList(500) { "test1-$it" }.plus(null)
        every { _file2Reader.readLine() } returnsMany MutableList(700) { "test2-$it" }.plus(null)

        // act
        val result = _reader.readFailedTestNames()

        // assert
        Assert.assertEquals(result.size, testRetryThreshold)
        verify(exactly = 1) { _file1Reader.close() }
        verify(exactly = 1) { _file2Reader.close() }
    }

    @Test
    fun `should remove reports directory on cleanup`() {
        // arrange
        every { _fileSystemMock.remove(any()) } returns true

        // act
        _reader.cleanup()

        // assert
        _fileSystemMock.let {
            verify(exactly = 1) { it.isExists(_directoryPath) }
            verify(exactly = 1) { it.isDirectory(_directoryPath) }
            verify(exactly = 1) { it.remove(_directoryPath) }
            confirmVerified(_fileSystemMock)
        }
    }
}