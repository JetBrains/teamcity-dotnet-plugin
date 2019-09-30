package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CustomArgumentsProviderTest {
    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_ARGUMENTS to "arg1 arg2"),
                        listOf("arg1", "arg2")))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val argumentsProvider = CustomArgumentsProvider(ParametersServiceStub(parameters), ArgumentsServiceStub())
        val ctx = Mockery()
        val context = DotnetBuildContext(ToolPath(Path("wd")), ctx.mock(DotnetCommand::class.java))

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}