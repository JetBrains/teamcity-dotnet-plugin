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
                arrayOf(MSBuildParameter("param", "value"), "/p:param=value"),
                arrayOf(MSBuildParameter("param.#!/aBc", "value"), "/p:param____aBc=value"),
                arrayOf(MSBuildParameter("param.aBc", "value"), "/p:param_aBc=value"),
                arrayOf(MSBuildParameter("param abC", "value"), "/p:param_abC=value"),
                arrayOf(MSBuildParameter("  ", "value"), "/p:__=value"),
                arrayOf(MSBuildParameter("", "value"), "/p:=value"),
                arrayOf(MSBuildParameter("123_param", "value"), "/p:123_param=value"),

                // escaped
                arrayOf(MSBuildParameter("param", "!@#\$%^&*()_+~1234-=/;'][{}\":<>,.?/??~`"), "/p:param=\"%21%40%23%24%%5E%26%2A%28%29%5F%2B%7E1234%2D%3D%2F;%27%5D%5B%7B%7D%22%3A%3C%3E%2C%2E%3F%2F%3F%3F%7E%60\""),
                arrayOf(MSBuildParameter("param", "value 123"), "/p:param=value%20123"),
                arrayOf(MSBuildParameter("param", "value \" 123"), "/p:param=value%20%22%20123"),
                arrayOf(MSBuildParameter("param", "value \\ 123"), "/p:param=value%20%5C%20123"),
                arrayOf(MSBuildParameter("param", "value \"\" 123"), "/p:param=value%20%22%22%20123"),
                arrayOf(MSBuildParameter("param", "value \" \" 123"), "/p:param=value%20%22%20%22%20123"),
                arrayOf(MSBuildParameter("param", "value1 \n value2"), "/p:param=value1%20%0A%20value2"),
                arrayOf(MSBuildParameter("param", "value1 \r value2"), "/p:param=value1%20%0D%20value2"),
                arrayOf(MSBuildParameter("param", "value1 \t value2"), "/p:param=value1%20%09%20value2"),
                arrayOf(MSBuildParameter("param", "value1 \b value2"), "/p:param=value1%20%08%20value2"),

                // should not escape `;` and should wrap a parameter by double quotes in this case (https://github.com/JetBrains/teamcity-dotnet-plugin/issues/144)
                arrayOf(MSBuildParameter("param", "Value;123"), "/p:param=\"Value;123\""),

                // empty
                arrayOf(MSBuildParameter("", ""), "/p:=\"\""))
    }

    @Test(dataProvider = "testData")
    fun shouldConvertToString(
            parameter: MSBuildParameter,
            expectedString: String) {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        val actualString = converter.convert(parameter)

        // Then
        Assert.assertEquals(actualString, expectedString)
    }
}