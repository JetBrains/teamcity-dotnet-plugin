package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.SplitTestsFilterProvider
import jetbrains.buildServer.dotnet.SplitTestsFilterSettings
import jetbrains.buildServer.dotnet.SplittedTestsFilterType
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesReader
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class SplitTestsFilterProviderTest {
    @MockK
    private lateinit var _settingsMock: SplitTestsFilterSettings

    @MockK
    private lateinit var _fileSystemMock: FileSystemService

    @MockK
    private lateinit var _testsNamesReaderMock: SplitTestsNamesReader

    @MockK
    private lateinit var _loggerMock: Logger

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkObject(Logger)
        every { Logger.getLogger(any()) } returns _loggerMock
        justRun { _loggerMock.debug(any<String>()) }
        justRun { _loggerMock.warn(any<String>()) }
    }

    @Test
    fun `should provide an empty filter expression if test classes file is null`() {
        // arrange
        every { _settingsMock.testsClassesFile } answers { null }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "")
        verify (exactly = 0) { _loggerMock.debug(any<String>()) }
        verify (exactly = 0) { _loggerMock.warn(any<String>()) }
    }

    @Test
    fun `should provide an empty filter expression if file doesn't exist`() {
        // arrange
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _fileSystemMock.isExists(any()) } answers { false }
        every { _fileSystemMock.isFile(any()) } answers { true }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "")
        verify (exactly = 1) { _loggerMock.warn(any<String>()) }
        verify (exactly = 1) { _loggerMock.debug(any<String>()) }
    }

    @Test
    fun `should provide an empty filter expression if file is not a file`() {
        // arrange
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _fileSystemMock.isExists(any()) } answers { true }
        every { _fileSystemMock.isFile(any()) } answers { false }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "")
        verify (exactly = 1) { _loggerMock.warn(any<String>()) }
        verify (exactly = 1) { _loggerMock.debug(any<String>()) }
    }

    @Test
    fun `should provide default filter expression for includes filter type`() {
        // arrange
        every { _fileSystemMock.isExists(any()) } answers { true }
        every { _fileSystemMock.isFile(any()) } answers { true }
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplittedTestsFilterType.Includes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "FullyQualifiedName~Namespace.TestClass0. | FullyQualifiedName~Namespace.TestClass1.")
    }

    @Test
    fun `should provide default filter expression for excluded filter type`() {
        // arrange
        every { _fileSystemMock.isExists(any()) } answers { true }
        every { _fileSystemMock.isFile(any()) } answers { true }
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplittedTestsFilterType.Excludes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "FullyQualifiedName!~Namespace.TestClass0. & FullyQualifiedName!~Namespace.TestClass1.")
    }

    @Test
    fun `should provide default filter expression for more than 1000 included test classes`() {
        // arrange
        every { _fileSystemMock.isExists(any()) } answers { true }
        every { _fileSystemMock.isFile(any()) } answers { true }
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplittedTestsFilterType.Includes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2500) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$").matches(result))
    }

    @Test
    fun `should provide default filter expression for more than 1000 excluded test classes`() {
        // arrange
        every { _fileSystemMock.isExists(any()) } answers { true }
        every { _fileSystemMock.isFile(any()) } answers { true }
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplittedTestsFilterType.Excludes }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2100) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\$").matches(result))
    }

    @Test
    fun `should provide exact match filter expression`() {
        // arrange
        every { _fileSystemMock.isExists(any()) } answers { true }
        every { _fileSystemMock.isFile(any()) } answers { true }
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _settingsMock.useExactMatchFilter } answers { true }
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(2, 2) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(
            result,
            "FullyQualifiedName=Namespace.TestClass0.Test0 | FullyQualifiedName=Namespace.TestClass0.Test1 " +
                    "| FullyQualifiedName=Namespace.TestClass1.Test0 | FullyQualifiedName=Namespace.TestClass1.Test1"
        )
    }

    @Test
    fun `should provide exact math filter expression for more than 1000 test names`() {
        // arrange
        every { _fileSystemMock.isExists(any()) } answers { true }
        every { _fileSystemMock.isFile(any()) } answers { true }
        every { _settingsMock.testsClassesFile } answers { mockk() }
        every { _settingsMock.useExactMatchFilter } answers { true }
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(25, 100) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$").matches(result))
    }

    private fun create() =
            SplitTestsFilterProvider(_settingsMock, _fileSystemMock, _testsNamesReaderMock)

    private fun generateTestClassesList(n: Int) = sequence {
        for (index in 0 until n) {
            yield("Namespace.TestClass$index")
        }
    }.toList()

    private fun generateTestsNamesList(n: Int, m: Int) = sequence {
        for (i in 0 until n) {
            for (j in 0 until m) {
                yield("Namespace.TestClass$i.Test$j")
            }
        }
    }
}
