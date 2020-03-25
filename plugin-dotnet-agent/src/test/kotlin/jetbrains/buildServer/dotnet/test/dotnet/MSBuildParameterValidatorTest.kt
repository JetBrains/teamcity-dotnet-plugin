package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.every
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.dotnet.EnvironmentVariablesImpl
import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.MSBuildParameterValidatorImpl
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildParameterValidatorTest {
    @DataProvider(name = "cases")
    fun osTypesData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(MSBuildParameter("name", "value"), true),
                arrayOf(MSBuildParameter("Name", "Value"), true),
                // special
                arrayOf(MSBuildParameter("_Name", "Value"), true),
                arrayOf(MSBuildParameter("Na_me", "Value"), true),
                arrayOf(MSBuildParameter("Na_me_", "Value"), true),
                arrayOf(MSBuildParameter("Na-me-", "Value"), true),
                arrayOf(MSBuildParameter("Na_m88e-9", "Value"), true),
                // empty
                arrayOf(MSBuildParameter("", ""), false),
                arrayOf(MSBuildParameter("name", ""), false),
                arrayOf(MSBuildParameter("", "value"), false),
                // first symbol
                arrayOf(MSBuildParameter("1name", "value"), false),
                arrayOf(MSBuildParameter("=name", "value"), false),
                arrayOf(MSBuildParameter("-name", "value"), false),
                arrayOf(MSBuildParameter("@name", "value"), false),
                arrayOf(MSBuildParameter("+name", "value"), false),
                arrayOf(MSBuildParameter("-name", "value"), false),
                arrayOf(MSBuildParameter("#name", "value"), false),
                arrayOf(MSBuildParameter("\$name", "value"), false),
                // other symbols
                arrayOf(MSBuildParameter("name#", "value"), false),
                arrayOf(MSBuildParameter("name^aa", "value"), false),
                arrayOf(MSBuildParameter("name&aa", "value"), false),
                arrayOf(MSBuildParameter("name*aa", "value"), false),
                arrayOf(MSBuildParameter("name=aa", "value"), false),
                arrayOf(MSBuildParameter("name!", "value"), false),
                arrayOf(MSBuildParameter("name>", "value"), false),
                arrayOf(MSBuildParameter("name<", "value"), false),
                arrayOf(MSBuildParameter("name?", "value"), false))
    }

    @Test(dataProvider = "cases")
    fun shouldProvideDefaultVarsWhenVirtualContextFromWindows(param: MSBuildParameter, expectedValue: Boolean) {
        // Given
        val validator = MSBuildParameterValidatorImpl()

        // When
        val actualValue = validator.isValid(param)

        // Then
        Assert.assertEquals(expectedValue, actualValue)
    }
}