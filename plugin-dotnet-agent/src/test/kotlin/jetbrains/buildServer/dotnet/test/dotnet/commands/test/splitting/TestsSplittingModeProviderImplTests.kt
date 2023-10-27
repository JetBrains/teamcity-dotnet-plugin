package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitting

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_USE_SUPPRESSION
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingModeProviderImpl
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettings
import jetbrains.buildServer.utils.getBufferedReader
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestsSplittingModeProviderImplTests {
    @MockK private lateinit var _parametersServiceMock: ParametersService
    @MockK private lateinit var _testsSplittingSettingsMock: TestsSplittingSettings

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkStatic(File::getBufferedReader)
    }

    @Test
    fun `should provide disabled mode if test classes file parameter not found`() {
        // arrange
        every { _testsSplittingSettingsMock.testsClassesFilePath } answers { null }
        val settings = create()

        // act
        val result = settings.getMode(Version.Empty)

        // assert
        Assert.assertEquals(TestsSplittingMode.Disabled, result)
    }

    @Test
    fun `should provide default mode if test classes file parameter found`() {
        // arrange
        every { _testsSplittingSettingsMock.testsClassesFilePath } answers { "included.txt" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER) } answers { "false" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_SUPPRESSION) } answers { "false" }
        val settings = create()

        // act
        val result = settings.getMode(Version.Empty)

        // assert
        Assert.assertEquals(TestsSplittingMode.TestClassNameFilter, result)
    }

    @Test
    fun `should be in test name filter mode if exact match flag is 'true'`() {
        // arrange
        every { _testsSplittingSettingsMock.testsClassesFilePath } returns "included.txt"
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER) } answers { "true" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_SUPPRESSION) } answers { "false" }
        val settings = create()

        // act
        val result = settings.getMode(Version.Empty)

        // assert
        Assert.assertEquals(TestsSplittingMode.TestNameFilter, result)
    }

    @Test
    fun `should be in test class filter mode if exact match flag is not 'true'`() {
        // arrange
        every { _testsSplittingSettingsMock.testsClassesFilePath } returns "included.txt"
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER) } answers { "  INVALID " }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_SUPPRESSION) } answers { "false" }
        val settings = create()

        // act
        val result = settings.getMode(Version.Empty)

        // assert
        Assert.assertEquals(TestsSplittingMode.TestClassNameFilter, result)
    }

    @Test
    fun `should provide default mode if test suppressor dotnet version requirement fails`() {
        // arrange
        every { _testsSplittingSettingsMock.testsClassesFilePath } returns "included.txt"
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER) } answers { "false" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_SUPPRESSION) } answers { "true" }
        val settings = create()

        // act
        val notSupportedDotnetVersion = Version(3, 1)
        val result = settings.getMode(notSupportedDotnetVersion)

        // assert
        Assert.assertEquals(TestsSplittingMode.TestClassNameFilter, result)
}

    @Test
    fun `should provide default mode if there are too few test classes`() {
        // arrange
        every { _testsSplittingSettingsMock.testsClassesFilePath } returns "included.txt"
        every { _testsSplittingSettingsMock.hasEnoughTestClassesToActivateSuppression } returns false
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER) } answers { "false" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_SUPPRESSION) } answers { "true" }
        val settings = create()

        // act
        val supportedDotnetVersion = Version(6, 0)
        val result = settings.getMode(supportedDotnetVersion)

        // assert
        Assert.assertEquals(TestsSplittingMode.TestClassNameFilter, result)
    }

    @DataProvider(name = "suppression parameter value and result test splitting mode")
    fun `versions of true string`() = arrayOf(
        arrayOf("true", TestsSplittingMode.Suppression),
        arrayOf("TRUE", TestsSplittingMode.Suppression),
        arrayOf("  TrUe     ", TestsSplittingMode.Suppression),
        arrayOf("false", TestsSplittingMode.TestClassNameFilter),
        arrayOf("FALSE", TestsSplittingMode.TestClassNameFilter),
        arrayOf("   fAlSe  ", TestsSplittingMode.TestClassNameFilter),
        arrayOf("  ", TestsSplittingMode.Suppression),
        arrayOf(" INVALID ", TestsSplittingMode.Suppression),
        arrayOf("1", TestsSplittingMode.Suppression),
        arrayOf("0", TestsSplittingMode.Suppression),
    )

    @Test(dataProvider = "suppression parameter value and result test splitting mode")
    fun `should be in suppress mode if test suppressor requirements are met`(suppressionParamValue: String, expectedMode: TestsSplittingMode) {
        // arrange
        every { _testsSplittingSettingsMock.testsClassesFilePath } returns "included.txt"
        every { _testsSplittingSettingsMock.hasEnoughTestClassesToActivateSuppression } returns true
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER) } answers { "false" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_SUPPRESSION) } returns suppressionParamValue
        val settings = create()

        // act
        val supportedDotnetVersion = Version(6, 0)
        val result = settings.getMode(supportedDotnetVersion)

        // assert
        Assert.assertEquals(result, expectedMode)
    }

    private fun create() = TestsSplittingModeProviderImpl(_parametersServiceMock, _testsSplittingSettingsMock)
}
