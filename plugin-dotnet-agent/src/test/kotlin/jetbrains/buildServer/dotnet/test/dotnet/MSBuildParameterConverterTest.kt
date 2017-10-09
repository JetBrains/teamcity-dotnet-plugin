package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.MSBuildParameterConverterImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildParameterConverterTest {
    @DataProvider
    fun testData(): Array<Array<out Any>> {
        return arrayOf(
                arrayOf(MSBuildParameter("param", "value"), "/p:param=\"value\""),
                arrayOf(MSBuildParameter("param.#!/aBc", "value"), "/p:param____aBc=\"value\""),
                arrayOf(MSBuildParameter("param.aBc", "value"), "/p:param_aBc=\"value\""),
                arrayOf(MSBuildParameter("param abC", "value"), "/p:param_abC=\"value\""),
                arrayOf(MSBuildParameter("  ", "value"), "/p:__=\"value\""),
                arrayOf(MSBuildParameter("", "value"), "/p:=\"value\""),
                arrayOf(MSBuildParameter("123_param", "value"), "/p:123_param=\"value\""),
                arrayOf(MSBuildParameter("param", "value 123"), "/p:param=\"value 123\""),
                arrayOf(MSBuildParameter("param", "value \" 123"), "/p:param=\"value \\\" 123\""),
                arrayOf(MSBuildParameter("param", "value \\ 123"), "/p:param=\"value \\\\ 123\""),
                arrayOf(MSBuildParameter("param", "value \"\" 123"), "/p:param=\"value \\\"\\\" 123\""),
                arrayOf(MSBuildParameter("param", "value \" \" 123"), "/p:param=\"value \\\" \\\" 123\""),
                arrayOf(MSBuildParameter("", ""), "/p:=\"\""))
    }

    @Test(dataProvider = "testData")
    fun shouldConvertToString(
            parameter: MSBuildParameter,
            expectedString: String) {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        var actualString = converter.convert(parameter)

        // Then
        Assert.assertEquals(actualString, expectedString)
    }
}