package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildArgumentsProviderTest {
    private lateinit var _context: DotnetBuildContext
    @MockK private lateinit var _msBuildParameterConverter: MSBuildParameterConverter
    @MockK private lateinit var _msBuildParametersProvider: MSBuildParametersProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        _context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
    }

    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("/p:param=value"))
        )
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(expectedArguments: List<String>) {
        // Given
        val msBuildParameter = MSBuildParameter("Param1", "Value1")
        every { _msBuildParametersProvider.getParameters(_context) } returns sequenceOf(msBuildParameter)
        every { _msBuildParameterConverter.convert(match { it.toList().equals(listOf(msBuildParameter)) }) } returns sequenceOf("/p:param=value")
        val argumentsProvider = MSBuildArgumentsProvider(_msBuildParameterConverter, listOf(_msBuildParametersProvider))

        // When
        val actualArguments = argumentsProvider.getArguments(_context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}