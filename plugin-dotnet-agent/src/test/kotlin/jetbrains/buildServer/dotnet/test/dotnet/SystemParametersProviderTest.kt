package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.SystemParametersProvider
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

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
        val provider = SystemParametersProvider(parametersService)

        // When
        val actualParameters = provider.parameters.toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}