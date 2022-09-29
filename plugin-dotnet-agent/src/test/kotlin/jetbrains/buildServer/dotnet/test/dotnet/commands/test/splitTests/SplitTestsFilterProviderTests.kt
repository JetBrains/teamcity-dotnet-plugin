package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitTests

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterSettings
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterType
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsNamesReader
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class SplitTestsFilterProviderTests {
    @MockK
    private lateinit var _settingsMock: SplitTestsFilterSettings

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
    fun `should provide an empty filter expression if test spiltting disabled`() {
        // arrange
        every { _settingsMock.isActive } answers { false }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertEquals(result, "")
        verify (exactly = 0) { _loggerMock.debug(any<String>()) }
        verify (exactly = 0) { _loggerMock.warn(any<String>()) }
    }

    @Test
    fun `should provide default filter expression for includes filter type`() {
        // arrange
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplitTestsFilterType.Includes }
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
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplitTestsFilterType.Excludes }
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
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplitTestsFilterType.Includes }
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
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { false }
        every { _settingsMock.filterType } answers { SplitTestsFilterType.Excludes }
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
        every { _settingsMock.isActive } answers { true }
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
        every { _settingsMock.isActive } answers { true }
        every { _settingsMock.useExactMatchFilter } answers { true }
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(25, 100) }
        val provider = create()

        // act
        val result = provider.filterExpression

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$").matches(result))
    }

    private fun create() =
            SplitTestsFilterProvider(_settingsMock, _testsNamesReaderMock)

    private fun generateTestClassesList(n: Int) = sequence {
        for (index in 0 until n) {
            yield("Namespace.TestClass$index")
        }
    }

    private fun generateTestsNamesList(n: Int, m: Int) = sequence {
        for (i in 0 until n) {
            for (j in 0 until m) {
                yield("Namespace.TestClass$i.Test$j")
            }
        }
    }
}
