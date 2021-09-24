package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.ParameterTestsFilterProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ParameterTestsFilterProviderTest {
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "testData")
    fun testData(): Any? {
        return arrayOf(
                arrayOf("Abc", "Abc"),
                arrayOf("  ", ""),
                arrayOf("", ""),
                arrayOf(null, "")
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFilter(filterParamValue: String?, expecedFilter: String) {
        // Given
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_CASE_FILTER) } returns filterParamValue
        val provider = createInstance()

        // When
        val actulFilter = provider.filterExpression;

        // Then
        Assert.assertEquals(actulFilter, expecedFilter)
    }

    private fun createInstance() = ParameterTestsFilterProvider(_parametersService)
}