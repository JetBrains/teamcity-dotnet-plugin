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
                arrayOf(MSBuildParameter("param", "value 123"), "/p:param=value%20123"),
                arrayOf(MSBuildParameter("param", "value \" 123"), "/p:param=value%20%22%20123"),
                arrayOf(MSBuildParameter("param", "value \\ 123"), "/p:param=value%20%5C%20123"),
                arrayOf(MSBuildParameter("param", "value \"\" 123"), "/p:param=value%20%22%22%20123"),
                arrayOf(MSBuildParameter("param", "value \" \" 123"), "/p:param=value%20%22%20%22%20123"),
                arrayOf(MSBuildParameter("param", "value1 \n value2"), "/p:param=value1%20%0A%20value2"),
                arrayOf(MSBuildParameter("param", "value1 \r value2"), "/p:param=value1%20%0D%20value2"),
                arrayOf(MSBuildParameter("param", "value1 \t value2"), "/p:param=value1%20%09%20value2"),
                arrayOf(MSBuildParameter("param", "value1 \b value2"), "/p:param=value1%20%08%20value2"),

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