

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitting

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.splitting.*
import jetbrains.buildServer.dotnet.commands.test.splitting.TestClassParametersProcessingMode.*
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesReader
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestsFilterProviderTestsSplitting {
    @MockK
    private lateinit var _settingsMock: TestsSplittingSettings

    @MockK
    private lateinit var _testsNamesReaderMock: TestsSplittingByNamesReader

    @MockK
    private lateinit var _loggerMock: Logger

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkObject(Logger)
        every { Logger.getLogger(any()) } returns _loggerMock
        every { _settingsMock.testClassParametersProcessingMode } returns EscapeSpecialCharacters
        justRun { _loggerMock.debug(any<String>()) }
        justRun { _loggerMock.warn(any<String>()) }
    }

    @Test
    fun `should provide an empty filter expression if test splitting disabled`() {
        // arrange
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.Disabled)

        // assert
        Assert.assertEquals(result, "")
        verify(exactly = 0) { _loggerMock.debug(any<String>()) }
        verify(exactly = 0) { _loggerMock.warn(any<String>()) }
    }


    @DataProvider
    fun testDataForDefaultFilterExpression() = arrayOf(
        arrayOf(
            TestsSplittingFilterType.Includes,
            sequenceOf("Namespace.TestClass0", "Namespace.TestClass1"),
            "FullyQualifiedName~Namespace.TestClass0. | FullyQualifiedName~Namespace.TestClass1."
        ),
        arrayOf(
            TestsSplittingFilterType.Excludes,
            sequenceOf("Namespace.TestClass0", "Namespace.TestClass1"),
            "FullyQualifiedName!~Namespace.TestClass0. & FullyQualifiedName!~Namespace.TestClass1."
        ),
    )

    @Test(dataProvider = "testDataForDefaultFilterExpression")
    fun `should provide test class filter expression`(
        filterType: TestsSplittingFilterType,
        names: Sequence<String>,
        expectedFilterExpression: String
    ) {
        // arrange
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.testClasses } answers { names }
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.TestClassNameFilter)

        // assert
        Assert.assertEquals(result, expectedFilterExpression)
    }

    @DataProvider
    fun testDataForDefaultFilterExpressionWithMoreThan1000TestClasses() = arrayOf(
        arrayOf(TestsSplittingFilterType.Includes, "^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$"),
        arrayOf(TestsSplittingFilterType.Excludes, "^\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\\s{1}&\\s{1}\\(.+\\)\$"),
    )

    @Test(dataProvider = "testDataForDefaultFilterExpressionWithMoreThan1000TestClasses")
    fun `should provide test class filter expression for more than 1000 test classes`(
        filterType: TestsSplittingFilterType,
        regex: String
    ) {
        // arrange
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.testClasses } answers { generateTestClassesList(2500) }
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.TestClassNameFilter)

        // assert
        Assert.assertTrue(Regex(regex).matches(result))
    }


    @Test
    fun `should provide test name filter expression`() {
        // arrange
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(2, 2) }
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.TestNameFilter)

        // assert
        Assert.assertEquals(
            result,
            "FullyQualifiedName=Namespace.TestClass0.Test0 | FullyQualifiedName=Namespace.TestClass0.Test1 " +
                    "| FullyQualifiedName=Namespace.TestClass1.Test0 | FullyQualifiedName=Namespace.TestClass1.Test1"
        )
    }

    @Test
    fun `should provide test name filter expression for more than 1000 test names`() {
        // arrange
        every { _testsNamesReaderMock.read() } answers { generateTestsNamesList(25, 100) }
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.TestNameFilter)

        // assert
        Assert.assertTrue(Regex("^\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\\s{1}\\|\\s{1}\\(.+\\)\$").matches(result))
    }

    @DataProvider
    fun testDataForParametrisedTestClasses() = arrayOf(
        // no parameters
        arrayOf(
            TestsSplittingFilterType.Includes,
            sequenceOf("TestClass0", "TestClass1"),
            "FullyQualifiedName~TestClass0. | FullyQualifiedName~TestClass1.",
        ),
        arrayOf(
            TestsSplittingFilterType.Excludes,
            sequenceOf("TestClass0", "TestClass1"),
            "FullyQualifiedName!~TestClass0. & FullyQualifiedName!~TestClass1.",
        ),
        // one string parameter
        arrayOf(
            TestsSplittingFilterType.Includes,
            sequenceOf("""TestClass0("param")""", """TestClass1("param")"""),
            """FullyQualifiedName~TestClass0\(\"param\"\) | FullyQualifiedName~TestClass1\(\"param\"\)""",
        ),
        arrayOf(
            TestsSplittingFilterType.Excludes,
            sequenceOf("""TestClass0("param")""", """TestClass1("param")"""),
            """FullyQualifiedName!~TestClass0\(\"param\"\) & FullyQualifiedName!~TestClass1\(\"param\"\)""",
        ),
        // multiple different parameters with all possible special characters
        arrayOf(
            TestsSplittingFilterType.Includes,
            sequenceOf("""TestClass0("param",42,"\\()&|=!~")""", """TestClass1"""),
            """FullyQualifiedName~TestClass0\(\"param\"%2C42%2C\"\\\\\(\)\&\|\=\!\~\"\) | FullyQualifiedName~TestClass1.""",
        ),
        arrayOf(
            TestsSplittingFilterType.Excludes,
            sequenceOf("""TestClass0("param",42,"\\()&|=!~")""", """TestClass1"""),
            """FullyQualifiedName!~TestClass0\(\"param\"%2C42%2C\"\\\\\(\)\&\|\=\!\~\"\) & FullyQualifiedName!~TestClass1.""",
        ),
    )

    @Test(dataProvider = "testDataForParametrisedTestClasses")
    fun `should provide test class filter expression processing test class parameters`(
        filterType: TestsSplittingFilterType,
        classNames: Sequence<String>,
        expectedFilterExpression: String
    ) {
        // arrange
        every { _settingsMock.filterType } answers { filterType }
        every { _settingsMock.testClasses } answers { classNames }
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.TestClassNameFilter)

        // assert
        Assert.assertEquals(result, expectedFilterExpression)
    }

    @DataProvider
    fun testDataParametersProcessingModes() = arrayOf(
        arrayOf(
            Trim,
            sequenceOf("""TestClass("param")"""),
            "FullyQualifiedName~TestClass.",
        ),
        arrayOf(
            NoProcessing,
            sequenceOf("""TestClass("param")"""),
            """FullyQualifiedName~TestClass("param")""",
        ),
    )

    @Test(dataProvider = "testDataParametersProcessingModes")
    fun `should provide test class filter expression processing test class parameters`(
        mode: TestClassParametersProcessingMode,
        classNames: Sequence<String>,
        expectedFilterExpression: String,
    ) {
        // arrange
        every { _settingsMock.testClassParametersProcessingMode } returns mode
        every { _settingsMock.filterType } answers { TestsSplittingFilterType.Includes }
        every { _settingsMock.testClasses } answers { classNames }
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.TestClassNameFilter)

        // assert
        Assert.assertEquals(result, expectedFilterExpression)
    }

    @DataProvider
    fun testDataForTrimmingTestClassParameters() = arrayOf(
        arrayOf("TestClass", "FullyQualifiedName~TestClass."),
        arrayOf("TestClass(param", "FullyQualifiedName~TestClass(param."),
        arrayOf("Namespace.TestClass", "FullyQualifiedName~Namespace.TestClass."),
        arrayOf("Namespace.TestClass(param", "FullyQualifiedName~Namespace.TestClass(param."),
        arrayOf("Namespace.TestClass(param1,param2)", "FullyQualifiedName~Namespace.TestClass\\(param1%2Cparam2\\)"),
        arrayOf("Namespace1.Namespace2.TestClass(param1,param2)", "FullyQualifiedName~Namespace1.Namespace2.TestClass\\(param1%2Cparam2\\)"),
        arrayOf("Namespace.TestClass((param))", "FullyQualifiedName~Namespace.TestClass\\(\\(param\\)\\)"),
        arrayOf("Namespace.TestCla(ss(param)", "FullyQualifiedName~Namespace.TestCla\\(ss\\(param\\)"),
    )

    @Test(dataProvider = "testDataForTrimmingTestClassParameters")
    fun `should provide test class filter expression processing test class parameters`(
        sourceName: String,
        expectedFilterExpression: String
    ) {
        // arrange
        every { _settingsMock.filterType } answers { TestsSplittingFilterType.Includes }
        every { _settingsMock.testClasses } answers { sequenceOf(sourceName) }
        val provider = create()

        // act
        val result = provider.getFilterExpression(TestsSplittingMode.TestClassNameFilter)

        // assert
        Assert.assertEquals(result, expectedFilterExpression)
    }

    private fun create() =
        TestsSplittingFilterProvider(_settingsMock, _testsNamesReaderMock)

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