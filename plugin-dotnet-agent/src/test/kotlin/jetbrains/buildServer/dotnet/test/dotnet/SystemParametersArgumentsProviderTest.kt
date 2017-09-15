package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.SystemParametersArgumentsProvider
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SystemParametersArgumentsProviderTest {
    @DataProvider
    fun normalizePropertyData(): Array<Array<String>> {
        return arrayOf(
                arrayOf("build.number", "buildNumber"),
                arrayOf("build.number.10_1", "buildNumber10_1"),
                arrayOf("b.n", "bN"),
                arrayOf("b", "b"),
                arrayOf("bUild", "bUild"),
                arrayOf("build_number", "build_number"),
                arrayOf("", ""),
                arrayOf("   ", ""),
                arrayOf(" . ++=", ""))
    }

    @Test(dataProvider = "normalizePropertyData")
    fun shouldNormalizePropertyName(
            targetPropertyName: String,
            expectedPropertyName: String) {
        // Given

        // When
        val actualPropertyName = SystemParametersArgumentsProvider.normalizePropertyName(targetPropertyName)

        // Then
        Assert.assertEquals(actualPropertyName, expectedPropertyName)
    }

    @Test
    fun shouldProvideArguments() {
        // Given
        val argumentsProvider = SystemParametersArgumentsProvider(ParametersServiceStub(mapOf("param1" to "value1", "param2" to "value2", "my.param3" to "value3")))

        // When
        val actualArgs = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArgs, listOf("/p:param1=value1", "/p:param2=value2", "/p:myParam3=value3"))
    }
}