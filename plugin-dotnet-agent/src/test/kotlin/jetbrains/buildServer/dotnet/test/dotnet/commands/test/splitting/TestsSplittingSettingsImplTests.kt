

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitting

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_CURRENT_BATCH
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_INCLUDES_FILE
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_SUPPRESSION_TEST_CLASSES_THRESHOLD
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PARALLEL_TESTS_USE_SUPPRESSION
import jetbrains.buildServer.dotnet.commands.test.splitting.TestClassParametersProcessingMode
import jetbrains.buildServer.dotnet.commands.test.splitting.TestClassParametersProcessingMode.*
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingSettingsImpl
import jetbrains.buildServer.utils.getBufferedReader
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.BufferedReader
import java.io.File

class TestsSplittingSettingsImplTests {
    @MockK private lateinit var _parametersServiceMock: ParametersService
    @MockK private lateinit var _fileSystemServiceMock: FileSystemService

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkStatic(File::getBufferedReader)
    }

    @Test
    fun `should provide use exact match filter size if parameter set`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_CURRENT_BATCH) } answers { "2" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.System, PARAM_PARALLEL_TESTS_INCLUDES_FILE) } answers { "file" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER) } answers { "  true " }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE) } answers { "  42 " }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_USE_SUPPRESSION) } answers { " faLse " }
        val settings = create()

        // act
        val result = settings.exactMatchFilterSize

        // assert
        Assert.assertEquals(result, 42)
    }

    @Test
    fun `should provide use exact match filter default size in parameter unset`() {
        // arrange
        every {
            _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE)
        } answers { " INVALID   " }
        val settings = create()

        // act
        val result = settings.exactMatchFilterSize

        // assert
        Assert.assertEquals(result, 10_000)
    }

    @Test
    fun `should provide test classes list from excludes or includes file`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_CURRENT_BATCH) } answers { "2" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.System, any()) } answers { "tmp" }

        val readerMock = mockk<BufferedReader>()
        every { readerMock.ready() } returnsMany(listOf(true, true, true, true, true, true, true, true, true, false))
        every { readerMock.readLine() } returnsMany(listOf(
            "#version=1",
            "  #SFSD ",
            "#algorithm=test",
            "#total_batches=2",
            "#suite=suite1",
            "  Namespace.TestClass0  ",
            "   Namespace.TestClass1 ",
            "#something",
            "Namespace.TestClass2",
        ))
        justRun { readerMock.close() }

        val fileMock = mockk<File>()
        every { fileMock.getBufferedReader() } answers { readerMock }
        every { _fileSystemServiceMock.getExistingFile(any()) } answers { Result.success(fileMock) }
        val settings = create()

        // act
        val result = settings.testClasses.toList()

        // assert
        Assert.assertEquals(result.size, 3)
        listOf("Namespace.TestClass0", "Namespace.TestClass1", "Namespace.TestClass2").forEach {
            Assert.assertTrue(result.contains(it))
        }
    }

    @DataProvider
    fun testDataSuppressionActivation(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf(99,  100, false),
            arrayOf(100, 100, true),
            arrayOf(1000, 100, true)
        )
    }

    @Test(dataProvider = "testDataSuppressionActivation")
    fun `should not allow suppression activation when number of test classes is low`(linesCount: Int, threshold: Int, expectedResult: Boolean) {
        // arrange
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_CURRENT_BATCH) } answers { "2" }
        every { _parametersServiceMock.tryGetParameter(ParameterType.Configuration, PARAM_PARALLEL_TESTS_SUPPRESSION_TEST_CLASSES_THRESHOLD) } answers { threshold.toString() }
        every { _parametersServiceMock.tryGetParameter(ParameterType.System, any()) } answers { "tmp" }

        val chars = generateSequence { '\n'.code }.take(linesCount).plus(-1).toList()
        val readerMock = mockk<BufferedReader>()
        every { readerMock.read() } returnsMany (chars)
        justRun { readerMock.close() }

        val fileMock = mockk<File>()
        every { fileMock.getBufferedReader() } answers { readerMock }
        every { _fileSystemServiceMock.getExistingFile(any()) } answers { Result.success(fileMock) }
        val settings = create()

        // act
        val result = settings.hasEnoughTestClassesToActivateSuppression

        // assert
        Assert.assertEquals(expectedResult, result)
        verify(atMost = threshold) { readerMock.read() }
    }

    @DataProvider
    fun testDataClassParametersProcessingModes() = arrayOf(
        arrayOf("Trim", Trim),
        arrayOf(" Trim ", Trim),
        arrayOf("trim", Trim),
        arrayOf("TRIM", Trim),
        arrayOf("NoProcessing", NoProcessing),
        arrayOf(" NoProcessing ", NoProcessing),
        arrayOf("noprocessing", NoProcessing),
        arrayOf("NOPROCESSING", NoProcessing),
        arrayOf(null, EscapeSpecialCharacters),
        arrayOf("EscapeSpecialCharacters", EscapeSpecialCharacters),
        arrayOf(" EscapeSpecialCharacters ", EscapeSpecialCharacters),
    )

    @Test(dataProvider = "testDataClassParametersProcessingModes")
    fun `should provide 'classParametersProcessingMode' feature toggle`(
        toggleValue: String?,
        expectedMode: TestClassParametersProcessingMode,
    ) {
        // arrange
        every {
            _parametersServiceMock.tryGetParameter(
                ParameterType.Configuration,
                DotnetConstants.PARAM_PARALLEL_TESTS_CLASS_PARAMETERS_PROCESSING_MODE
            )
        } answers { toggleValue }
        val settings = create()

        // act
        val result = settings.testClassParametersProcessingMode

        // assert
        Assert.assertEquals(result, expectedMode)
    }

    private fun create() = TestsSplittingSettingsImpl(_parametersServiceMock, _fileSystemServiceMock)
}