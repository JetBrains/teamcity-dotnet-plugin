package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.MSBuildParameter
import jetbrains.buildServer.dotnet.MSBuildParameterConverterImpl
import jetbrains.buildServer.dotnet.MSBuildParameterType
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildParameterConverterTest {
    @DataProvider
    fun casesNames(): Array<Array<String>> {
        return arrayOf(
                arrayOf("name", "name"),
                arrayOf("Name", "Name"),
                // special
                arrayOf("_Name", "_Name"),
                arrayOf("Na_me", "Na_me"),
                arrayOf("Na_me_", "Na_me_"),
                arrayOf("Na-me-", "Na-me-"),
                arrayOf("Na_m88e-9", "Na_m88e-9"),
                // first symbol
                arrayOf("1name", "_name"),
                arrayOf("=name", "_name"),
                arrayOf("-name", "_name"),
                arrayOf("@name", "_name"),
                arrayOf("+name", "_name"),
                arrayOf("#name", "_name"),
                arrayOf("\$name", "_name"),
                // other symbols
                arrayOf("name#", "name_"),
                arrayOf("name.aa", "name_aa"),
                arrayOf("name.Aa", "name_Aa"),
                arrayOf("name Aa", "name_Aa"),
                arrayOf("name^aa", "name_aa"),
                arrayOf("name&aa", "name_aa"),
                arrayOf("name*aa", "name_aa"),
                arrayOf("name=aa", "name_aa"),
                arrayOf("name!", "name_"),
                arrayOf("name>", "name_"),
                arrayOf("name<", "name_"),
                arrayOf("name?", "name_"))
    }

    @Test(dataProvider = "casesNames")
    fun shouldNormalizeName(name: String, expectedName: String) {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        val actualName = converter.normalizeName(name)

        // Then
        Assert.assertEquals(expectedName, actualName)
    }

    @DataProvider
    fun valueCases(): Array<Array<Any>> {
        return arrayOf(
                // escaped
                arrayOf("value 123", MSBuildParameterType.Unknown, "\"value 123\""),
                arrayOf("value123\\", MSBuildParameterType.Unknown, "value123\\\\"),
                arrayOf("value 123\\", MSBuildParameterType.Unknown, "\"value 123\\\\\""),
                arrayOf("value 123\\ ", MSBuildParameterType.Unknown, "\"value 123\\ \""),
                arrayOf("value123\\a", MSBuildParameterType.Unknown, "value123\\a"),
                arrayOf("value 123\\a", MSBuildParameterType.Unknown, "\"value 123\\a\""),
                arrayOf("value 123  \\a", MSBuildParameterType.Unknown, "\"value 123  \\a\""),
                arrayOf("value \" 123", MSBuildParameterType.Unknown, "\"value \\\" 123\""),
                arrayOf("value \"\" 123", MSBuildParameterType.Unknown, "\"value \\\"\\\" 123\""),
                arrayOf("value \" \" 123", MSBuildParameterType.Unknown, "\"value \\\" \\\" 123\""),
                arrayOf("value1 \n value2", MSBuildParameterType.Unknown, "\"value1 %0A value2\""),
                arrayOf("value1\rvalue2", MSBuildParameterType.Unknown, "\"value1%0Dvalue2\""),
                arrayOf("value1 \t value2", MSBuildParameterType.Unknown, "\"value1 %09 value2\""),
                arrayOf("value1 \b value2", MSBuildParameterType.Unknown, "\"value1 %08 value2\""),
                arrayOf("value1 , value2", MSBuildParameterType.Unknown, "\"value1 , value2\""),
                arrayOf("value1 , value2", MSBuildParameterType.Predefined, "\"value1 %2C value2\""),

                // should not escape `;` for response files and should wrap a parameter by double quotes in this case https://github.com/JetBrains/teamcity-dotnet-plugin/issues/144
                arrayOf("Value;123", MSBuildParameterType.Unknown, "\"Value;123\""),
                arrayOf("!@#\$%^&*()_+~1234-=/;'][{}\":<>,.?/??~`", MSBuildParameterType.Unknown, "\"!@#$%^&*()_+~1234-=/;'][{}\\\":<>,.?/??~`\""),

                // should  escape nod digits and non letters when predefined
                arrayOf("Value;123", MSBuildParameterType.Predefined, "\"Value%3B123\""),
                arrayOf("Value,123", MSBuildParameterType.Predefined, "\"Value%2C123\""),
                arrayOf("!@#\$%^&*()_+~1234-=/;'][{}\":<>,.?/~`", MSBuildParameterType.Predefined, "\"%21%40%23%24%%5E%26%2A()_%2B%7E1234-%3D/%3B%27%5D%5B%7B%7D\\\":%3C%3E%2C.%3F/%7E%60\"")
        )
    }

    @Test(dataProvider = "valueCases")
    fun shouldNormalizeValue(
            value: String,
            parameterType: MSBuildParameterType,
            expectedValue: String) {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        val actualValue = converter.normalizeValue(value) { char -> MSBuildParameterConverterImpl.shouledBeEscaped(parameterType, char) }

        // Then
        Assert.assertEquals(actualValue, expectedValue)
    }

    @Test
    fun shouldConvert() {
        // Given
        val converter = MSBuildParameterConverterImpl()

        // When
        val actualParams = converter.convert(
                sequenceOf(MSBuildParameter("d", "   "), MSBuildParameter("d", ""), MSBuildParameter("a", "b"), MSBuildParameter("", "ñ"), MSBuildParameter("  ", "c")))
                .toList()

        // Then
        Assert.assertEquals(actualParams, listOf("-p:a=b"))
    }
}