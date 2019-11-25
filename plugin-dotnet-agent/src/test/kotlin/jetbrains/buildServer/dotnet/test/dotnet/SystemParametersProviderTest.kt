package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.SystemParametersProvider
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SystemParametersProviderTest {
    @MockK
    private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
    }

    @DataProvider
    fun testData(): Array<Array<out Any>> {
        return arrayOf(
                arrayOf(ParametersServiceStub(mapOf("arg1" to "val1")), listOf(MSBuildParameter("arg1", "v_val1"))))
    }

    @Test(dataProvider = "testData")
    fun shouldProduceParameters(
            parametersService: ParametersService,
            expectedParameters: List<MSBuildParameter>) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val provider = SystemParametersProvider(parametersService, _virtualContext)

        // When
        val actualParameters = provider.getParameters(context).toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}