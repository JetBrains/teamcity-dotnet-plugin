package jetbrains.buildServer.dotnet.test.nunit.arguments

import io.mockk.*
import io.mockk.impl.annotations.MockK

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.nunit.NUnitSettings
import jetbrains.buildServer.nunit.arguments.NUnitTestFilterProvider

import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NUnitTestFilterProviderTest {
    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    data class TestCase(
        val expectedFilter: String,
        val shouldLogWarning: Boolean = false,
        val includeCategories: String? = null,
        val excludeCategories: String? = null,
        val additionalCommandLine: String? = null
    )

    @DataProvider(name = "testCases")
    fun getCases(): Array<TestCase> = arrayOf(
        TestCase(
            expectedFilter = ""
        ),
        TestCase(
            expectedFilter = "cat==A",
            includeCategories = "A"
        ),
        TestCase(
            expectedFilter = "cat==A||cat==B",
            includeCategories = "A,B"
        ),
        TestCase(
            expectedFilter = "cat!=Z",
            excludeCategories = "Z"
        ),
        TestCase(
            expectedFilter = "cat==A&&cat!=Z",
            includeCategories = "A",
            excludeCategories = "Z"
        ),
        TestCase(
            expectedFilter = "(cat==A||cat==B)&&cat!=Z&&cat!=X",
            includeCategories = "A,B",
            excludeCategories = "Z,X"
        ),
        TestCase(
            expectedFilter = "(cat==A||cat==B)&&cat!=Z&&cat!=X&&cat!=Y",
            includeCategories = "\nA\nB\n",
            excludeCategories = "Z\n\nX,Y"
        ),
        TestCase(
            expectedFilter = "(cat==A||cat==B||cat==C||cat==DD D)&&cat!=Z&&cat!=X&&cat!=Y",
            includeCategories = separator + "\nA\nB\n" + separator + "C" + separator + separator + "DD D",
            excludeCategories = "Z" + separator + "X,Y" + separator
        ),
        TestCase(
            expectedFilter = "",
            shouldLogWarning = true,
            includeCategories = "A",
            excludeCategories = "Z",
            additionalCommandLine = "--where"
        ),
        TestCase(
            expectedFilter = "",
            shouldLogWarning = true,
            includeCategories = "A",
            additionalCommandLine = "--where"
        ),
        TestCase(
            expectedFilter = "",
            shouldLogWarning = true,
            excludeCategories = "Z",
            additionalCommandLine = "--where"
        ),
        TestCase(
            expectedFilter = "cat==A",
            includeCategories = "A",
            additionalCommandLine = "where"
        ),
        TestCase(
            expectedFilter = "cat!=Z",
            excludeCategories = "Z",
            additionalCommandLine = "where"
        ),
        TestCase(
            expectedFilter = "cat==A&&cat!=Z",
            includeCategories = "A",
            excludeCategories = "Z",
            additionalCommandLine = "where"
        ),
        TestCase(
            expectedFilter = "cat==A",
            includeCategories = "A",
            additionalCommandLine = "abc"
        ),
        TestCase(
            expectedFilter = "cat!=Z",
            excludeCategories = "Z",
            additionalCommandLine = "abc"
        ),
        TestCase(
            expectedFilter = "cat==A&&cat!=Z",
            includeCategories = "A",
            excludeCategories = "Z",
            additionalCommandLine = "abc"
        ),
        TestCase(
            expectedFilter = "cat==A",
            includeCategories = "A",
            additionalCommandLine = ""
        ),
        TestCase(
            expectedFilter = "cat!=Z",
            excludeCategories = "Z",
            additionalCommandLine = ""
        ),
        TestCase(
            expectedFilter = "cat==A&&cat!=Z",
            includeCategories = "A",
            excludeCategories = "Z",
            additionalCommandLine = ""
        ),
        TestCase(
            expectedFilter = "",
            additionalCommandLine = "--where"
        ),
        TestCase(
            expectedFilter = "",
            additionalCommandLine = "abc"
        ),
        TestCase(
            expectedFilter = "",
            additionalCommandLine = ""
        ),
    )

    @Test(dataProvider = "testCases")
    fun `should provide nunit filter`(testCase: TestCase) {
        // arrange
        _nUnitSettings.run {
            every { includeCategories } returns testCase.includeCategories
            every { excludeCategories } returns testCase.excludeCategories
            every { additionalCommandLine } returns testCase.additionalCommandLine
        }
        justRun { _loggerService.writeWarning(any()) }

        val provider = NUnitTestFilterProvider(_nUnitSettings, ArgumentsServiceStub(), _loggerService)

        // act
        val filter = provider.filter

        // assert
        Assert.assertEquals(filter, testCase.expectedFilter)
        val warningsLogged = if (testCase.shouldLogWarning) 1 else 0
        verify(exactly = warningsLogged) { _loggerService.writeWarning(any()) }
    }

    companion object {
        private val separator = System.lineSeparator()
    }
}