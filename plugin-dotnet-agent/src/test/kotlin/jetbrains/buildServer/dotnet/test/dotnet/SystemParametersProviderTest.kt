package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class SystemParametersProviderTest {
    @DataProvider
    fun testData(): Array<Array<out Any>> {
        return arrayOf(
                arrayOf(ParametersServiceStub(mapOf("arg1" to "val1")), listOf(MSBuildParameter("arg1", "val1"))))
    }

    @Test(dataProvider = "testData")
    fun shouldProduceParameters(
            parametersService: ParametersService,
            expectedParameters: List<MSBuildParameter>) {
        // Given
        val ctx = Mockery()
        val context = DotnetBuildContext(ToolPath(Path("wd")), ctx.mock(DotnetCommand::class.java))
        val provider = SystemParametersProvider(parametersService)

        // When
        val actualParameters = provider.getParameters(context).toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}